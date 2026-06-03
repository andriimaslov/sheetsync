package dev.maslov.sheetsync.service.notification

import android.app.Notification
import android.os.Bundle
import android.service.notification.StatusBarNotification
import android.util.Log
import android.util.LruCache
import androidx.work.Operation
import androidx.work.WorkManager
import androidx.work.WorkRequest
import dev.maslov.sheetsync.model.Rule
import dev.maslov.sheetsync.service.rules.RuleRepository
import io.mockk.*
import java.time.LocalDateTime
import java.util.*
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Test

class NotificationListenerTest {

    private lateinit var listener: NotificationListener
    private val repository = mockk<RuleRepository>()
    private val workManager = mockk<WorkManager>()
    private val rulesFlow = MutableStateFlow<List<Rule>>(emptyList())

    @Before
    fun setUp() {
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
        every { Log.i(any(), any()) } returns 0

        mockkConstructor(LruCache::class)
        every { anyConstructed<LruCache<String, String>>().get(any<String>()) } answers { null }
        every { anyConstructed<LruCache<String, String>>().put(any<String>(), any<String>()) } answers { null }

        every { repository.rules } returns rulesFlow
        every { workManager.enqueue(any<WorkRequest>()) } returns mockk<Operation>()

        listener = NotificationListener()
        listener.repository = repository
        listener.workManager = workManager
    }

    @Test
    fun `normal notification - rule matches - enqueues work`() {
        val pkg = "com.example.app"
        val rule = createRule(pkg)
        setActiveRules(listOf(rule))

        val sbn = createMockSbn(pkg, "key1", "Some text", isSummary = false)

        listener.onNotificationPosted(sbn)

        verify { workManager.enqueue(any<WorkRequest>()) }
    }

    @Test
    fun `ignoring group summary - does not enqueue work`() {
        val pkg = "com.example.app"
        val rule = createRule(pkg)
        setActiveRules(listOf(rule))

        val sbn = createMockSbn(pkg, "key1", "Some text", isSummary = true)

        listener.onNotificationPosted(sbn)

        verify(exactly = 0) { workManager.enqueue(any<WorkRequest>()) }
    }

    @Test
    fun `ignoring duplicate - same key and text - enqueues only once`() {
        val pkg = "com.example.app"
        val rule = createRule(pkg)
        setActiveRules(listOf(rule))

        val sbn = createMockSbn(pkg, "key1", "Some text", isSummary = false)

        val cache = mutableMapOf<String, String>()
        val lruCacheMock = mockk<LruCache<String, String>>()
        every { lruCacheMock.get(any<String>()) } answers { cache[it.invocation.args[0] as String] }
        every { lruCacheMock.put(any<String>(), any<String>()) } answers
            {
                cache[it.invocation.args[0] as String] = it.invocation.args[1] as String
                null
            }

        setProcessedNotificationsCache(lruCacheMock)

        listener.onNotificationPosted(sbn)
        listener.onNotificationPosted(sbn)

        verify(exactly = 1) { workManager.enqueue(any<WorkRequest>()) }
    }

    @Test
    fun `different key same text - does NOT ignore - enqueues twice`() {
        val pkg = "com.example.app"
        val rule = createRule(pkg)
        setActiveRules(listOf(rule))

        val cache = mutableMapOf<String, String>()
        val lruCacheMock = mockk<LruCache<String, String>>()
        every { lruCacheMock.get(any<String>()) } answers { cache[it.invocation.args[0] as String] }
        every { lruCacheMock.put(any<String>(), any<String>()) } answers
            {
                cache[it.invocation.args[0] as String] = it.invocation.args[1] as String
                null
            }
        setProcessedNotificationsCache(lruCacheMock)

        val sbn1 = createMockSbn(pkg, "key1", "Same Text", isSummary = false)
        val sbn2 = createMockSbn(pkg, "key2", "Same Text", isSummary = false)

        listener.onNotificationPosted(sbn1)
        listener.onNotificationPosted(sbn2)

        verify(exactly = 2) { workManager.enqueue(any<WorkRequest>()) }
    }

    @Test
    fun `rule not matched - does not enqueue work`() {
        val pkg = "com.example.app"
        setActiveRules(emptyList())

        val sbn = createMockSbn(pkg, "key1", "Some text", isSummary = false)

        listener.onNotificationPosted(sbn)

        verify(exactly = 0) { workManager.enqueue(any<WorkRequest>()) }
    }

    @Test
    fun `notification update with different text - enqueues work again`() {
        val pkg = "com.example.app"
        val rule = createRule(pkg)
        setActiveRules(listOf(rule))

        // Setup cache mock
        val cache = mutableMapOf<String, String>()
        val lruCacheMock = mockk<LruCache<String, String>>()
        every { lruCacheMock.get(any<String>()) } answers { cache[it.invocation.args[0] as String] }
        every { lruCacheMock.put(any<String>(), any<String>()) } answers
            {
                cache[it.invocation.args[0] as String] = it.invocation.args[1] as String
                null
            }

        setProcessedNotificationsCache(lruCacheMock)

        val sbn1 = createMockSbn(pkg, "key1", "Text 1", isSummary = false)
        val sbn2 = createMockSbn(pkg, "key1", "Text 2", isSummary = false)

        listener.onNotificationPosted(sbn1)
        listener.onNotificationPosted(sbn2)

        verify(exactly = 2) { workManager.enqueue(any<WorkRequest>()) }
    }

    private fun setActiveRules(rules: List<Rule>) {
        val field = NotificationListener::class.java.getDeclaredField("activeRules")
        field.isAccessible = true
        field.set(listener, rules)
    }

    private fun setProcessedNotificationsCache(cache: LruCache<String, String>) {
        val field = NotificationListener::class.java.getDeclaredField("processedNotifications")
        field.isAccessible = true
        field.set(listener, cache)
    }

    private fun createRule(pkg: String) = Rule(
        id = UUID.randomUUID(),
        title = "Test Rule",
        isActive = true,
        createdAt = LocalDateTime.now(),
        sheetId = "sheet123",
        sheetName = "Sheet 1",
        tabName = "Tab 1",
        lastRunStatus = "",
        lastRunAt = null,
        appId = pkg,
        parser = "parser"
    )

    private fun createMockSbn(pkg: String, key: String, text: String?, isSummary: Boolean): StatusBarNotification {
        val sbn = mockk<StatusBarNotification>()
        val notification = mockk<Notification>()
        val extras = mockk<Bundle>()

        every { sbn.packageName } returns pkg
        every { sbn.key } returns key
        every { sbn.notification } returns notification

        notification.flags = if (isSummary) Notification.FLAG_GROUP_SUMMARY else 0
        notification.extras = extras

        every { extras.getCharSequence(Notification.EXTRA_TEXT) } returns text

        return sbn
    }
}
