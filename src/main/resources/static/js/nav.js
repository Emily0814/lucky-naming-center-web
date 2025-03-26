document.addEventListener('DOMContentLoaded', function() {
            //네이게이션 바 가져오기
            const navContainer = document.querySelector('.nav-container');
            const navSpacer = document.querySelector('.nav-spacer');
            
            //네비게이션 바의 초기 오프셋 위치 가져오기
            const navbarOffset = navContainer.offsetTop;
            
            //스크롤 이벤트를 처리하는 함수
            function handleScroll() {
                if (window.pageYOffset >= navbarOffset) {
                    //스크롤 위치에 도달하면 스티키 클래스를 추가
                    navContainer.classList.add('sticky');
                    //콘텐츠 점프를 방지하려면 스페이서를 표시
                    navSpacer.style.display = 'block';
                    navSpacer.style.height = navContainer.offsetHeight + 'px';
                } else {
                    //스크롤 위치를 벗어나면 스티키 클래스를 제거
                    navContainer.classList.remove('sticky');
                    //스페이서 숨기기
                    navSpacer.style.display = 'none';
                }
            }
            
            //스크롤에 대한 이벤트 리스너를 추가
            window.addEventListener('scroll', handleScroll);
        });