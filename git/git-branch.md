### branch

- 브랜치는 git 저장소의 특정 시점에서 작업을 분리하여 독립적으로 개발을 진행할 수 있도록 도와주는 도구이다.

- 실제 현업에서 dev, staging, main 등 여러 공용 브랜치 관리
  - 일반적으로 production 관련 브랜치는 main, 개발용 브랜치는 dev
- task별로 개별 브랜치 생성하여 사용
  - 팀 컨벤션 설정해서 브랜치명 설정
  - 현업 브랜치 생성 및 통합 작업 순서
    - local에서 main 브랜치 기준으로 feature 브랜치 생성
    - 작업 후 git push origin feature
    - Pull Request(PR)을 통해 dev까지 merge
    - dev에서 main으로 최종 merge
    - 작업 완료된 브랜치 삭제



### branch 관련 주요 커맨드

#### git branch

- 현재 로컬 저장소에 있는 모든 브랜치 목록
- `git branch -a` : 원격 branch 정보까지 모두 조회

- `git branch 브랜치명`
  - 로컬에 새로운 브랜치를 생성하는 명령어
  - 기존에 checkout 되어있던 브랜치 commit base로 신규 브랜치를 생성
- `git branch -D 브랜치명` : 해당 브랜치 삭제
  - 다른 브랜치로 checkout 한 후에 삭제대상 브랜치를 삭제해야 한다.
- `git push origin --delete 브랜치명` 
  - 원격지 브랜치 삭제
  - 또는 github에서 직접 삭제도 가능 (권장)

#### git checkout

- `git checkout 브랜치명` : 현재 브랜치에서 다른 브랜치로 전환하는 명령어
- `git checkout -b 브랜치명` : 새 브랜치를 생성하고 해당 브랜치로 전환 (생성, 전환 동시)

#### git fetch

- 로컬 저장소로 특정 브랜치 정보를 fetch
- 모든 브랜치 fetch
  - `git fetch --all`
  - `git fetch --all --prune` : 삭제 브랜치 정보까지 fetch

#### 모든 브랜치 이력 포함 clone 및 새 원격주소로 push

`--mirror` 커맨드로 클론하고 레포 주소 변경한 다음 푸시

- `git clone --mirror 기존원격레포주소`
- `git remote set-url origin 새원격레포주소`
- `git push --mirror`





### git branch의 merge 전략

#### merge

- 두 브랜치의 변경 사항을 통합하는 기본적인 방법
- branch1에서 넘어온 commitID와 신규 merge commitID가 dev브랜치에 남게된다.
- 즉, 브랜치의 커밋 ID를 그대로 가져오고 추가로 merge commit이 발생한다.



#### rebase merge

- 한 브랜치의 커밋을 다른 브랜치의 최신 커밋에 **재적용** 하는 방식

- 이때는 브랜치에서 넘어온 commitID가 아닌, 새로운 commitID가 발급되어 main 브랜치에 생성

- **장점**

  - merge commitID는 남지 않게 되어 불필요한 커밋 없이 깔끔한 커밋 관리

- ##### 단점

  - 기존 comit history 자체는 유지되지만, 모든 commit ID가 변경됨으로서 이후에 동일 브랜치에서 재pr시 충돌이 발생한다. 따라서, 사용하던 branch는 더이상 사용이 어렵다.



#### squash merge

- squah는 여러 커밋을 하나의 커밋으로 합치는 과정이다.

- 로컬 저장소에서 여러 커밋을 발생시켰을 때 해당 커밋ID를 통합하여 하나의 commit ID로 만들어 dev에는 하나의 commitID로만 이력을 생성한다.

- ##### 장점

  - 불필요한 커밋 없이 깔끔한 커밋 관리

- ##### 단점

  - 기존에 사용하던 branch는 더이상 사용이 어렵고 세밀한 작업이 불가능하다.



#### merge 전략 요약

- `basic merge` : 기존 commitID 그대로 가져오고 추가적인 merge commit ID 발생
- `rebase merge` : 기존 commitID를 새로운 commitID로 변경해서 main 브랜치에 적용 (commit 개수 만큼)
- `squash merge` : 기존 여러 commitID를 새로운 commitID 1개로 통합해서 main 브랜치에 적용