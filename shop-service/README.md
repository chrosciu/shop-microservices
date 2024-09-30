# Wprowadzenie

Jest to wersja sklepu mająca na celu pokazać najprostszy setup klienta Config Server-a

# Konfiguracja

Oprócz podania url do Config Server-a ważne jest także wskazanie nazwy aplikacji (`spring.application.name`) - ustawiamy ją na `shop`. 

Na podstawie bowiem tej nazwy klient będzie prosił Config Server o propertiesy właśnie dla tej aplikacji. 

Nie podajemy także profilu - zatem użyty zostanie `default`

Zatem ostatecznie request będzie następujący:

```text
GET <config server url>/shop/default
```

# Reload propertiesów

W najprostszej wersji aby zmusić klienta do ponownego zaczytania propertiesów z Config Server-a należy wykorzystać Actuator a konkretnie jeden endpoint:

```text
POST /actuator/refresh
```

Po uderzeniu w ww. endpoint zostaną odświeżone te beany, które mają `@RefreshScope` - w naszym przypadku jest to `ShopController`. 

Dzięki temu zostanie na nowo wczytana zawartość pola `shopName` - używamy bowiem na nim adnotacji `@Value`.

**WAŻNE !!!**: Reload nie działa jeżeli wstrzykniemy property nie poprzez `@Value` - lecz poprzez `@ConfigurationProperties` - więcej szczegółów tutaj:

[https://github.com/spring-cloud/spring-cloud-commons/issues/846](https://github.com/spring-cloud/spring-cloud-commons/issues/846)

# Komunikacja z `greeting-service` za pomocą `FeignClient`

Bycie zarejestrowanym w Discovery Server-ze umożliwia nam komunikację pomiędzy mikroserwisami bez potrzeby używania zahardcodowanych adresów IP. 

Wykorzystamy zatem ten fakt do komunikacji z `greeting-service`.

Aby było to możliwe należy:
- w obu serwisach dodać zależność `spring-cloud-starter-netflix-eureka-client` 
- oraz dodać kilka podstawowych propertiesów (skopiowanych bezpośrednio z dokumentacji) by dało się połączyć z Eureka Server-em
- dodatkowo w `shop-service`:
    - dodajemy zależność do `spring-cloud-starter-openfeign` i adnotację `@EnableFeignClients`
    - definiujemy interfejs `GreetingClient` będący Feign-ową przekładką do `shop-service` - od tej pory można go wstrzykiwać jako beana 

**WAŻNE !!** Nazwa podane w adnotacji `@FeignClient` w interfejsie `GreetingClient` (czyli `greeting`) musi zgadzać się z property `spring.application.name` w `greeting-service`    