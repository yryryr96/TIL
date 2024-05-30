### HTML 폼 전송 방식



##### application/x-www-form-urlencoded

- HTML 폼 데이터를 서버로 전송하는 가장 기본적인 방법
- form 태그에 enctype 옵션이 없으면 웹 브라우저는 요청 HTTP 메시지 헤더에 `Content-Type: application/x-www-form-urlencoded`를 추가한다.
- 폼에 입력한 데이터를 HTTP Body에 문자로 `username=kim&age=20`과 같이 &로 구분해서 전송한다.



파일을 전송하기 위해서는 바이너리 데이터를 전송해야 한다.

문자로 전송하는 방식으로 파일을 전송하기는 어려움.

또한, 보통 폼 태그로 전송할 때 이름, 나이, 파일과 같이 문자와 바이너리 데이터를 같이 전송하는 경우가 많다.



##### multipart/form-data

이 방식을 사용하려면 form 태그에 별도로 `enctype="multipart/form-data"`를 지정해야 한다.

`multipart/form-data` 방식은 다른 종류의 여러 파일과 폼의 내용을 함께 전송할 수 있다.



HTTP 메시지 각각 전송 항목이 구분되어 있다. `Content-Disposition`이라는 항목별 헤더가 추가되어 있고 여기에 부가 정보가 있다.

파일의 경우 파일 이름과 Content-Type이 추가되고 바이너리 데이터가 전송된다.

````
multipart/form-data는 각 항목을 구분해서, 한번에 전송하는 것
````



각 항목은 `request.getParts()` 에 포함된 `Part` 인스턴스로 접근할 수 있다.



##### MultipartFile

HTTP form으로 itemName, file 을 보낸다고 가정하자.

```java
@PostMapping("/upload")
public String saveFile(@RequestParam String itemName, @RequestParam MultipartFile file, HttpServletRequest request) throws IOException {
    
    if (!file.isEmpty()) {
        String fullPath = fileDir + file.getOriginalFilename();
        file.transferTo(new File(fullPath));
    }
    
    return "upload-form";
}
```

`MultipartFile` 로 업로드 파일에 쉽게 접근할 수 있다. (**Dispatcher Servlet에서 MultipartResolver 실행**) 만약 Http form 태그에서 `multiple="multiple"` 속성을 설정해서 파일을 한 번에 여러 개 보낸다면 `List<MultipartFile>` 로 접근할 수 있다.



MultipartFile은 @RequestParam, @ModelAttribute로 바인딩 가능하다.