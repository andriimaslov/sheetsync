[![Build](https://github.com/andriimaslov/sheetsync/actions/workflows/build.yml/badge.svg)](https://github.com/andriimaslov/sheetsync/actions/workflows/build.yml)

# SheetSync

I needed a way to automatically push my bank transaction notifications to Google Sheets without manual entry. So I built this. 

It's a personal tool tailored for my specific bank notification formats (Privat24).

### How it works
1. **Listen**: Captures incoming notifications.
2. **Parse**: Extracts amount, description, and account info using custom parsers.
3. **Sync**: Appends data to a Google Sheet using the Sheets API.

### Tech & Architecture
- **UI**: Jetpack Compose with Material 3.
- **DI**: Hilt for dependency injection.
- **Async**: Coroutines & Flow for reactive state management.
- **Background**: WorkManager handles the Google Sheets API calls to ensure data is synced even if the network is spotty.
- **Patterns**: MVVM with Repository pattern.

---
*Built because manual budget tracking is awful.*
