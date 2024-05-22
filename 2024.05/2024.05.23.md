# 2024.05.23 TIL



### 시간, 날짜 클래스

직접 구현하기 어려움 -> 있는거 쓰자.



- **LocalDate**
  - 연,월,일
- **LocalTime**
  - 시,분,초
- **LocalDateTime**
  - 연,월,일,시,분,초

```java
LocalDate ld = LocalDate.of(2023, 1, 15); // 2023-01-15
LocalTime lt = LocalTime.of(13,30,59); // 13:30:59
LocalDateTime ldt = LocalDateTime.of(ld, lt); // 2023-01-15T13:30:59
```



- **ZonedDateTime**
  - ZoneId를 설정해서 다른 ZoneId를 가진 곳의 시간을 알기 위해 사용할 수 있음.
  - LocalDate
  - LocalTime
  - ZoneId ( ex. Asia/Seoul)

```java
LocalDateTime ldt = LocalDateTime.now();

ZonedDateTime seoulZdt = ZonedDateTime.of(ldt, ZoneId.of("Asia/Seoul")); // 서울 시간
ZonedDateTime nyZdt = ZonedDateTime.of(ldt, ZoneId.of("America/New_York")); // 뉴욕 시간
```



- **DateTimeFormatter**

시간, 날짜를 포맷팅, 파싱 해주는 클래스

```java
LocalDateTime now = LocalDateTime.of(2024,12,31,13,30,59);
DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

String formattedDateTime = now.format(formatter);
System.out.println(formattedDateTime); // 2024-12-31 13:30:59

String input = "2030-01-01 23:30:59";
LocalDateTime parsedDateTime = LocalDateTime.parse(input, formatter);
System.out.println(parsedDateTime); // 2030-01-01T23:30:59
```



- **ChronoUnit** vs **ChronoField**

`ChronoUnit`은 연,월,일 자체를 가리키는 것

`ChronoField`는 날짜에 있는 연,월,일을 가리키는 것

```java
// ChronoUnit
LocalDateTime dt = LocalDateTime.of(2018,1,1,13,30,59);

LocalDateTime plusDt1 = dt.plus(10, ChronoUnit.YEARS);
System.out.println(plusDt1); // 2028-01-01T13:30:59

LocalDateTime plusDt2 = dt.plusYears(10);
System.out.println(plusDt2); // 2028-01-01T13:30:59

// ChronoField
LocalDateTime dt = LocalDateTime.of(2018,1,1,13,30,59);

LocalDateTime changeDt1 = dt.with(ChronoField.YEAR, 2020); //with 불변객체 반환
System.out.println(changeDt1); // 2020-01-01T13:30:59

LocalDateTime changeDt2 = dt.withYear(2020);
System.out.println(changeDt2); // 2020-01-01T13:30:59
```



- **TemporalAdjusters**

날짜 관련 유용한 Utils

```java
LocalDateTime dt = LocalDateTime.now(); // 기준 날짜

LocalDateTime with1 = dt.with(TemporalAdjusters.next(DayOfWeek.FRIDAY)); // 다음 금요일
LocalDateTime with2 = dt.with(TemporalAdjusters.lastInMonth(DayOfWeek.SUNDAY)); // 이번 달 마지막 일요일
```



- **Period, Duration** (기간)

```java
Period period = Period.ofDays(10);

LocalDate currentDate = LocalDate.of(2030,1,1); // 2030-01-01
LocalDate plusDate = currentDate.plus(period); // 2030-01-11

LocalDate startDate = LocalDate.of(2023,1,1);
LocalDate endDate = LocalDate.of(2023,4,2);
Period between = Period.between(startDate, endDate);
System.out.println("기간: " + between.getMonths() + "개월 " + between.getDays() + "일");
// 기간: 3개월 1일
```

```java
Duration duration = Duration.ofMinutes(30);
LocalTime lt = LocalTime.of(1,0); // 01:00
LocalTime plusTime = lt.plus(duration); // 01:30

LocalTime start = LocalTime.of(9,0);
LocalTime end = LocalTime.of(10,0);
Duration between = Duration.between(start, end);
System.out.println("차이: " + between.getSeconds() + "초");
System.out.println("근무 시간: " + between.toHours() + "시간 " + between.toMinutesPart() + "분");
// toMinutesPart() : 시간을 제외하고 minute만 반환
```

