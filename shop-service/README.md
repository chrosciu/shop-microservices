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

