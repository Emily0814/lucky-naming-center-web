document.addEventListener('DOMContentLoaded', function() {
  // 비밀번호 표시/숨기기 토글 함수
  function setupPasswordToggle(toggleButtonId, passwordInputId, eyeIconId) {
    const toggleButton = document.getElementById(toggleButtonId);
    const passwordInput = document.getElementById(passwordInputId);
    const eyeIcon = document.getElementById(eyeIconId);
    
    if(toggleButton && passwordInput && eyeIcon) {
      toggleButton.addEventListener('click', function() {
        // 비밀번호 필드 타입 변경
        if (passwordInput.type === 'password') {
          passwordInput.type = 'text';
          eyeIcon.className = 'fa fa-eye-slash'; // 눈 모양 아이콘 변경
        } else {
          passwordInput.type = 'password';
          eyeIcon.className = 'fa fa-eye'; // 눈 모양 아이콘 변경
        }
      });
    }
  }
  
  // 비밀번호 토글 설정
  setupPasswordToggle('toggle-password', 'userPassword', 'eye-icon');
  setupPasswordToggle('toggle-confirm-password', 'confirmPassword', 'eye-confirm-icon');
});