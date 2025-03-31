document.addEventListener('DOMContentLoaded', function() {
    // 네비게이션 바 가져오기
    const navContainer = document.querySelector('.nav-container');
    const navSpacer = document.querySelector('.nav-spacer');
    
    if (navContainer && navSpacer) {
        // 네비게이션 바의 초기 오프셋 위치 가져오기
        const navbarOffset = navContainer.offsetTop;
        
        // 스크롤 이벤트를 처리하는 함수
        function handleScroll() {
            if (window.pageYOffset >= navbarOffset) {
                // 스크롤 위치에 도달하면 스티키 클래스를 추가
                navContainer.classList.add('sticky');
                // 콘텐츠 점프를 방지하려면 스페이서를 표시
                navSpacer.style.display = 'block';
                navSpacer.style.height = navContainer.offsetHeight + 'px';
            } else {
                // 스크롤 위치를 벗어나면 스티키 클래스를 제거
                navContainer.classList.remove('sticky');
                // 스페이서 숨기기
                navSpacer.style.display = 'none';
            }
        }
        
        // 스크롤에 대한 이벤트 리스너를 추가
        window.addEventListener('scroll', handleScroll);
    }
    
    // 햄버거 메뉴 아이콘 - 올바른 selector 사용
    const menuIcon = document.querySelector(".menu-icon"); // getElementById가 아닌 querySelector 사용
    const navItems = document.querySelector(".nav-items");
    if (menuIcon && navItems) {
        menuIcon.addEventListener("click", function () {
            console.log("Menu icon clicked");
            navItems.classList.toggle("show");
        });
    }

    // 회원가입 및 로그인 링크 관련 코드 제거
    // 이제 이 기능은 modal.js에서 처리합니다
	// URL 파라미터 확인
	    const urlParams = new URLSearchParams(window.location.search);
	    const needLogin = urlParams.get('needLogin');
	    
	    if (needLogin === 'true') {
	        // 로그인 모달창 표시
	        const loginModal = new bootstrap.Modal(document.getElementById('login-modal'));
	        loginModal.show();
	        
	        // 리다이렉트 URL이 있으면 저장 (로그인 성공 후 사용)
	        const redirectUrl = urlParams.get('redirectUrl');
	        if (redirectUrl) {
	            // 로그인 폼에 hidden 필드로 추가하거나 세션 스토리지에 저장
	            document.getElementById('redirect-url').value = redirectUrl;
	            // 또는
	            sessionStorage.setItem('redirectUrl', redirectUrl);
	        }
	        
	        // URL에서 파라미터 제거 (브라우저 히스토리 깔끔하게)
	        window.history.replaceState({}, document.title, '/');
	    }
	
	
});

