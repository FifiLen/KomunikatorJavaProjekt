# 📟 Komunikator w stylu GaduGadu

## 📄 Opis Projektu
Ten projekt to komunikator w stylu GaduGadu, stworzony w języku Java. Projekt ten został zrealizowany na potrzeby studiów, a jego celem jest umożliwienie użytkownikom komunikacji w czasie rzeczywistym. Serwer aplikacji jest uruchomiony w chmurze Google Cloud, co pozwala na dostęp do komunikatora z dowolnego miejsca. Baza danych również znajduje się w Google Cloud.

## ✨ Funkcjonalności
- 📝 Rejestracja użytkowników
- 🔑 Logowanie użytkowników
- ➕ Dodawanie znajomych
- ➖ Usuwanie znajomych
- 💬 Wysyłanie wiadomości między użytkownikami
- 📜 Wyświetlanie historii czatu
- 👥 Lista znajomych

## ☁️ Infrastruktura
- 🌐 Serwer uruchomiony na Google Cloud
- 💾 Baza danych na Google Cloud
- 🚫 Serwer nie działa lokalnie, lecz wyłącznie w chmurze

## 📋 Wymagania
- ☕ Java 8 lub nowsza
- 🗄️ Biblioteka MySQL Connector (mysql-connector-j-8.4.0.jar)

## 🚀 Uruchomienie klienta

1. **Skopiuj projekt z repozytorium GitHub**:
    ```bash
    git clone <URL-do-repozytorium>
    cd <nazwa-repozytorium>
    ```

2. **Kompilacja i uruchomienie klienta**:
    - Upewnij się, że masz zainstalowaną Javę 8 lub nowszą.
    - Skonfiguruj klaspath do biblioteki MySQL Connector:
      ```bash
      export CLASSPATH=.:libs/mysql-connector-j-8.4.0.jar:.
      ```
    - Skompiluj pliki źródłowe:
      ```bash
      javac client/ChatClient.java
      ```
    - Uruchom klienta:
      ```bash
      java client.ChatClient
      ```

## 👥 Autorzy
- Szymon Zimonczyk
- Filip Lendel
