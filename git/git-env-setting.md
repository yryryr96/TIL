### github 인증방법 2가지

1. `oauth 인증` 방식(제3자인증-웹로그인)
2. `pat token 인증`: github에서 직접 보안키를 발급받고 키체인(자격증명)에 등록

Settings -> Developer settings -> Personal access tokens -> Tokens(classic) 탭에서 생성할 수 있다.



### git 프로젝트 생성 방법 2가지

1. github에서 진행중인 원격 repo를 clone
2. 로컬에서부터 이미 진행중인 프로젝트를 github에 업로드



#### git init

해당 프로젝트 위치에 `.git`폴더 생성 (repo 주소, 사용자 정보 등 정보를 담고 있다)

#### git remote add origin 레포주소

원격지 주소를 생성 및 추가

#### git remote remove origin

원격지 주소 삭제

#### git remote set-url origin 레포주소

원격지 주소 변경

#### git config --list

git 설정정보 조회



### 타인 레포 clone 받는 방법 2가지

##### 커밋 이력 그대로 가져오기

- 타인 레포 클론 후 레포 주소를 나의 레포 주소로 변경
- `.git` 폴더에는 커밋 이력이 모두 남아있다.

1. `git clone 타인레포 주소`

2. `git remote set-url origin 나의레포주소`

3. `git push origin main`



##### 커밋 이력 없이 레포 가져오기

1. `git clone 타인레포 주소` 
2. `.git` 폴더 삭제 : 커밋 이력, 레포주소 등 삭제
3. `git init`
4. `git remote add origin 나의레포주소`
5. `git add .`
6. `git commit -m "커밋메시지"`
7. `git push origin main`



### 사용자 지정 방법

##### 전역 사용자 지정

- `git config --global user.name "유저네임"`
- `git config --global user.email "유저이메일"`

##### 지역 사용자 지정

- `git config --local user.name "유저네임"`
- `git config --local user.email "유저이메일"`

##### 사용자 정보 조회

- `git config user.name`
- `git config user.email`
- `git config --list`



#### .gitignore

- 특정 파일을 git 추적 목록에서 제외시키고 싶다면 .gitignore파일에 등록

- 이미 add, commit 된 파일을 추적 목록에서 제외하고 싶다면

`git rm -r --cached .` 캐시 삭제 커맨드 후 .gitignore 파일에 등록