# Wprowadzenie

Jest to wersja Config Servera mająca pokazać najprostszy setup propertiesów - składowanych zarówno na dysku (profil `native`) jak też w repozytorium Git (profil `git`)

# Składowanie na dysku - profil `native`

Aby uniknąć hardocodowania ścieżek propertiesy zostały umieszczone w katalogu `config` znajdującym się w `src\main\resources`. 

Zaletą tego podejścia jest przenośność - można bowiem odwołać się do takiego folderu poprzez `classpath`. 

Wadą jest niemożność prostego odświeżenia (folder bowiem pakowany jest do JARa i w runtime nie da się nim manipulować). 

Ponieważ jest to jednak rozwiązanie stricte developerskie można posłużyć się DevTools-ami ze Spring Boota. Wówczas aby zmienić propertiesy wystarczy dokonać update pliku `shop.yml` a następnie za pomocą opcji `Build => Recompile` (lub Ctrl + Shift + F9) dokonać rekompilacji pliku co spowoduje szybki restartu Springa i zaczytanie propertiesów na nowo.

# Składowanie w repozytorium - profil `git`

Tutaj sprawa jest dość prosta - podajemy `url` do repozytorium `https://github.com/chrosciu/shop-config`. 

Oprócz tego dość ważny jest parametr `timeout` ustawiony na `0` co powoduje każdorazowe sprawdzenie zawartości repozytorium gdy jakiś klient prosi o propertiesy. Dodatkowo dajemy `force-pull` na `true` tak aby clone repozytorium dokonywał się od razu przy starcie Config Server-a (fail fast)

**WAŻNE !!!**: W przypadku uruchamiania Config Server-a w systemie Windows konieczne jest aby do zmiennej środowiskowej `%PATH%` dodać folder zawierający plik `git.exe`. Zlekceważenie tego wymagania skutkuje mało oczywistymi błędami biblioteki JGit używanej pod spodem przez Config Server.  

# Odczyt propertiesów

W naszym serwerze zdefiniowaliśmy tylko jeden plik `shop.yml` zatem aby odczytać propertiesy musimy wykonać następujący request:

```text
GET /shop/default
```

Druga część ścieżki (`/default`) określa profil (a konkretnie jego brak - czyli profil domyślny). Częstym błędem jest bowiem niepodanie żadnego profilu w requeście:

```text
GET /shop
```

co skutkuje niespodziewanym statusem `404`.

# Monitorowanie zmian w repozytorium

Aby można było notyfikować Config Server o tym, że nastąpiła zmiana w repozytorium należy dołożyć do projektu dodatkową zależność `spring-cloud-config-monitor`

Monitor do działania wymaga aby w application context był zdefiniowany bean typu `BusBridge` reprezentujący szynę na którą mają zostać wysłane eventy by przepropagować zmiany do klientów. W pierwszej wersji można takiego bean zdefniować ręcznie - tak aby eventy szły tylko do logów

Po wykonaniu powyższych czynności będzie dostępny nowy endpoint:

```text
POST /monitor
```

# Hookdeck

Ponieważ nasza aplikacja jest nie jest dostępna "z zewnątrz" musimy wspomóc się zewnętrzym proxy tak aby GitHub mógł się do nas dostać - w tym celu użyjemy serwisu Hookdeck.  

Jego konfiguracja jest bardzo prosta - wystarczy użyć narzędzia `hookdeck-cli` by utworzyć tymczasowe proxy do naszego monitora. 

By uniknąć konieczności instalacji ww. narzędzia można uruchomić je jako kontener Dockerowy:

```bash
docker run --rm -it hookdeck/hookdeck-cli:v0.11.2 listen http://host.docker.internal:8071/monitor
```

Używamy nazwy hosta `host.docker.internal` by dostać się z kontenera do naszej maszyny.

W wyniku wywołania polecenia powinniśmy otrzymać URL w stylu `https://hkdk.events/<xyz>` - będzie on nam potrzebny na GitHubie przy deklarowaniu webhooka. 

Dostajemy także adres konsoli Hookdeck - można będzie sprawdzać w niej na bieżąco działanie webhooka.

**WAŻNE !!!**: Z powodów bezpieczeństw Hookdeck dokonuje normalizacji adresów URL - dlatego też zamienia `/monitor` na `/monitor/` (dodaje trailing slasha), co skutkuje statusem 404 gdy spróbujemy uderzyć w taki endpoint.

By rozwiązać ten problem niezbędne jest dodanie ekstra konfiguracji po stronie Springa (klasa `WebConfig`) i zawołanie w niej metody `setUseTrailingSlashMatch(true)`. 

Metoda ta jest oznaczona jako deprecated (zniknie w Springu 7) i należy ją zastąpić poprzez przekierowanie na poziomie filtra / kontrolera / proxy ale na razie tak to zostawiam. 

# Dodanie webhooka w GitHubie

W repozytorium wchodzimy w opcję `Settings ==> Webhooks ==> Add webhook`. 

Jako URL podajemy adres dostarczony przez Hookdeck, wyłączamy SSL i wybieramy by webhook wołał się tylko przy zdarzeniach `push`

By przetestować działanie webhooka modyfikujemy dowolny plik w repozytorium, następnie robimy commit i push.

# Automatyczny reload propertiesów u klientów po wykryciu zmian

By klienci automatyczne refreshowali propertiesy musimy skorzystać z jakiegoś mechanizmu kolejkowego i stworzyć tzw. szynę komunikacyjną (bus). 

Wówczas Config Server po notyfikacji webhookiem wyśle informację na szynę, a klienci po jej odebraniu dokonają reloadu propertiesów.

Spring Cloud zapewnia startery do dwóch brokerów - RabbitMQ i Kafki; my wykorzystamy ten pierwszy -w tym celu dokładamy zależność `spring-cloud-starter-bus-amqp`.

# Konfiguracja RabbitMQ

Jeżeli wystartujemy kontener RabbitMQ z domyślnymi ustawieniami (szczegoły w `docker-compose.yml`) to nie musimy dodatkowo konfigurować w Springu niczego więcej.

Domyślne ustawienia są następujące:

- user - `guest`
- pass - `guest`
- port - `5672`
- web gui port - `15672`

Bardzo istotna jest ostatnia rzecz - web GUI do zarządzania brokerem (dostępne jest w przeglądarce pod adresem `http://localhost:15672`). Z tego względu należy wybrać kontener RabbitMQ z tagiem zawierającym `management`

# Jak to działa w runtime ?

Po wystartowaniu RabbitMQ i ConfigServer-a flow wygląda mniej więcej tak:

- ConfigServer podłącza się do RabbitMQ
- tworzony jest jeden exchange o nazwie `springCloudBus`
- tworzone jest tyle kolejek ile jest klientów (**UWAGA**: ConfigServer też jest klientem szyny!)
- kolejki podłączają sie do ww. exchange
- po odbraniu webhooka ConfigServer wysyła wiadomość do exchange, które z kolei dystrybuuje je do wszystkich kolejek

Po stronie klientów Config Server-a jest z kolei tak:

- klient również musi mieć dodaną zależność `spring-cloud-starter-bus-amqp`
- klient po swoim starcie również podłącza się do szyny (i tworzona jest dla niego kolejka)
- jeśli taki klient odbierze wiadomość z kolejki to sprawdza czy nazwa aplikacji zawarta w wiadomości zgadza się z jego nazwą aplikacji
- jeśli tak jest - to wówczas dokonuje reloadu propertiesów