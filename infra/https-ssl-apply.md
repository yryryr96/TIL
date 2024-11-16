### HTTPS 적용 

EC2 서버에 SSL을 발급하고 https를 적용해서 443 포트로 들어오는 요청을 nginx로 제어하고자 한다.

1. 클라이언트가 443 포트로 접속
2. nginx에서 요청에 따라 http로 server1, server2와 통신

**이와 같은 흐름을 설정한 이유는 nginx에서 https 요청을 처리하면 백엔드 서버에서 https와 관련된 처리를 하지 않아도 된다.**

| SSL 인증서를 발급받기 위해서는 domain이 있어야한다.



##### SSL 인증서 발급

ec2환경에서  `letsencrypt`로 무료로 쉽게 SSL을 발급받을 수 있다.

단, 짧은 갱신 기간을 가지고 있기 때문에 90일 마다 SSL을 갱신해야 한다.



1. certbot 설치

```
sudo yum install certbot
```

2. certbot을 사용해 SSL 인증서 발급

```
sudo certbot certonly --standalone -d <domain>
```

이와 같이 SSL 인증서를 발급받으면 ec2의 `/etc/letsencrypt/live/<domain>/` 경로에 `fullchain.pem`, `privchain.pem` 이 생성된다.



##### docker-compose.yml

```dockerfile
services:
  web:
    image: nginx
    restart: always
    ports:
      - 80:80
      - 443:443
    depends_on:
      - server1
      - server2
    volumes:
      - ./nginx/default.conf:/etc/nginx/conf.d/default.conf
      - /etc/letsencrypt/:/etc/letsencrypt/ #ec2 local에 있는 key를 nginx container에 마운트 -> nginx에서 ssl 처리를 해야하기 때문 
    networks:
      - app-network
```



##### default.conf

```nginx
# 백엔드 upstream 설정
upstream server1-app {
    server server1:8080;
}

upstream server2-app {
    server server2:8080;
}

server {

    listen 443 ssl;
    server_name evan523.shop;

    # ssl 인증을 ec2 local에서 마운트한 key로 진행
    ssl_certificate /etc/letsencrypt/live/evan523.shop/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/evan523.shop/privkey.pem;

    # 443 포트로 들어온 https 요청을 nginx -> server로는 http 요청으로 라우팅
    location /server1 {
        proxy_pass http://server1-app/health;
    }

    location /server2 {
        proxy_pass http://server2-app/health;
    }
}

# 80 포트로 들어오는 요청들은 https 요청으로 변경해서 라우팅
server {
    listen 80;
    server_name evan523.shop;

    return 301 https://$host$request_uri;
}
```



### 회고

- SSL 인증서는 EC2 local에, SSL 인증은 nginx container에서 진행했기 때문에 mount 하는 과정에서 권한이 없어서 mount가 되지 않아 SSL 인증이 진행되지 않았다.
- 따라서, nginx container에서 EC2 local의 /etc/letsencrypt 경로에 접근할 수 있는 권한을 부여하고 정상적으로 mount 된다면 SSL 인증은 쉽게 해결된다.