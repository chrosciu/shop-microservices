# Wprowadzenie

Najprostsza wersja Discovery Server-a (Netflix Eureka) pozwalająca na rejestrację mikroserwisów 

# Konfiguracja

Aby uruchomić Discovery Server dodajemy zależność `spring-cloud-starter-netflix-eureka-server` i adnotację `@EnableEurekaServer`

Z przedstawionych propertiesów najważniejsze znaczenie ma jeden (`registerWithEureka`). Nie chcemy bowiem, aby serwer rejestrował się sam w sobie.

Reszta ustawień jest skopiowana bezpośrednio z dokumentacji projektu.