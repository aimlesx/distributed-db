# Rozproszona Baza Danych

## Struktura Bazy
Baza składa się z węzłów, które wspólnie formują nieskierowany graf. W tego typu strukturze mogą tworzyć się cykle, które wymagają specjalnej uwagi. Niektóre operacje na bazie przypominają algorytm DFS (Depth First Search), który w swojej czystej postaci na grafie cyklicznym może wykonywać się rekurencyjnie w nieskończoność, efektywnie uniemożliwiając funkcjonowanie bazy danych.

## Protokół
Protokół używany w bazie danych jest protokołem bezstanowym.

### Struktura komunikatów
```
[id zadania] <operacja> [argumenty operacji]
```
- operacja - jest to operacja realizowana na bazie danych zlecona przez klienta
- argumenty operacji - opcjonalnie są to dodatkowe parametry operacji
- id zadania - UUID operacji, liczba unikalnie reprezentująca daną operację

### ID Zadania
W komunikacji klient-węzeł id zadania nie jest wymagane, co sprawia że klient może je pominąć bez żadnych implikacji. Węzeł który odbiera komunikat, sprawdza czy ma on swój identyfikator i w sytuacji jego nieobecności generuje go. Węzły przed propagacją komunikatu do innych węzłów zapisują sobie jego identyfikator, tak aby być w stanie stwierdzić, czy ten sam komunikat nie dotarł do nich kolejny raz tworząc cykl. W przypadku wykrycia cyklu, węzeł zamyka połączenie i nie procesuje dalej tej operacji. Węzły pamiętają identyfikatory przez 60 sekund.

### Możliwe operacje
- set-value
  Ustawia nową wartość klucza.
  *Składnia*: ```set-value <klucz>:<wartość>```
  *Odpowiedź*: ```OK``` jeżeli wartość zostanie ustawiona, ```ERROR``` jeżeli w bazie nie ma węzła przechowującego podany klucz

- get-value
  Wyszukuje rekord z podanym kluczem.
  *Składnia*: ```get-value <klucz>```
  *Odpowiedź*: ```<klucz>:<wartość>``` jeżeli klucz zostanie odnaleziony, ```ERROR``` jeżeli w bazie nie ma węzła przechowującego podany klucz

- find-key
  Wyszukuje węzeł, który przechowuje podany klucz.
  *Składnia*: ```find-key <klucz>```
  *Odpowiedź*: ```<adres IP>:<port>``` jeżeli węzeł przechowujący rekord z podanym kluczem istnieje, ```ERROR``` jeżeli w bazie nie ma węzła przechowującego podany klucz

- get-max
  Wyszukuje rekord z największą wartością.
  *Składnia*: ```get-max```
  *Odpowiedź*: ```<klucz>:<wartość>```

- get-min
  Wyszukuje rekord z najmniejszą wartością.
  *Składnia*: ```get-min```
  *Odpowiedź*: ```<klucz>:<wartość>```

- new-record
  Ustawia nowy rekord przechowywany przez węzeł, który wykonuje tą operację.
  *Składnia*: ```new-record <klucz>:<wartość>```
  *Odpowiedź*: ```OK```

- terminate
  Wyłącza węzeł wykonujący tą operację, uprzednio odłączając go od sieci.
  *Składnia*: ```terminate```
  *Odpowiedź*: ```OK```

#### Specjalne Operacje
Specjalne operacje są używane przez węzły w celu organizacji struktury bazy danych. **Nie powinny być one używane przez klienta.**
- handshake
  Informuje węzeł o tym, że inny węzeł chce się z nim połączyć.
  *Składnia*: ```handshake <adres IP nadawcy>:<port nadawcy>```
  *Odpowiedź*: **Brak**

- bye
  Informuje węzeł o tym, że inny węzeł chce się od niego odłączyć.
  *Składnia*: ```bye <adres IP nadawcy>:<port nadawcy>```
  *Odpowiedź*: **Brak**

## Wielowątkowość
Procesy węzła posiadają dynamiczną pulę wątków, która dostosowuje się do obciążenia danego węzła, dzięki czemu jeden węzeł jest w stanie przetwarzać wiele operacji jednocześnie.

## Kompilacja i Uruchomienie
### Kompilacja
- Manualna
```cmd
:: W głównym folderze projektu
javac -d out src/*.java
```
- Przy użyciu skryptu kompilującego
```cmd
:: W głównym folderze projektu
.\compile.bat
```
### Uruchomienie węzła bazy danych
> Aby uruchomić węzeł musisz znajdować się w folderze ze skompilowanymi plikami `.class` lub ustawić odpowiedni `CLASSPATH`.
```
java DatabaseNode -tcpport <port> -record <key>:<value> [<neighbour ip>:<neighbour port> ...]
```
*\<port\>* - port na którym będzie nasłuchiwać uruchamiany węzeł
*\<key\>:\<value\>* - klucz oraz przechowywana pod nim wartość
*\<neighbour ip\>:\<neighbour port\>* - pary adresów IP i portów sąsiadujących węzłów

### Uruchomienie klienta
> Aby uruchomić aplikację klienta musisz znajdować się w folderze ze skompilowanymi plikami `.class` lub ustawić odpowiedni `CLASSPATH`.
```
java Client -gateway <ip>:<port> -operation <operation> [<args>]
```
*\<ip\>:\<port\>* - adres IP oraz port węzła któremu klient zleci wykonanie podanej operacji
*\<operation\>* - operacja do wykonania na bazie danych
*\[<port\>]* - opcjonalne argumenty operacji

### Uruchomienie przykładowych testów
W głównym katalogu projektu znajduje się skrypt zawierający przykładowe testy. Tworzy on strukturę sieci widoczną na obrazie poniżej. Testuje głównie czy z każdego punktu sieci widać całą sieć oraz sprawdza poprawność możliwych operacji.
```cmd
:: W głównym folderze projektu
.\test.bat
```

***Uwagi:***
- Skrypt testowy należy uruchomić po uprzednim skompilowaniu projektu
- Skrypt testowy wykorzystuje załączonego klienta bazy danych (```Client.java```)

## Co zostało zaimplementowane
- Możliwość organizacji sieci tworzącej bazę danych (inkrementalne podłączanie węzłów oraz ich terminacja)
- Wszystkie wyżej wymienione operacje
- Detekcja cykli
- Wielowątkowość (możliwość przetwarzania wielu operacji jednocześnie, czyli możliwość istnienia wielu klientów korzystających z bazy jednocześnie)
  Czyli chyba wszystko co zostało wymienione w specyfikacji.