![image-20250217232844378](../images/image-20250217232844378.png)

- #### work space

  - 현재 프로젝트 작업하는 공간

- #### staging 

  - commit 될 목록들이 저장되는 공간
  - `git add` 된 변경 사항이 있는 파일들이 저장되는 공간

- #### Local repository

  - `git commit` 커맨드로 staging area의 변경사항을 확정짓고 commit ID를 생성한다.
  - commit이 되면 Local Repository에 저장된다.

- #### remote repository

  - remote repository에 local repository의 변경사항(commit ID)을 업로드
    - `git push origin main/master`
  - remote repository의 변경사항을 local repository에 다운로드
    - `git pull`