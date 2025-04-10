// mypage.js

document.addEventListener('DOMContentLoaded', function() {
    // 프로필 수정 버튼 클릭
    const editProfileBtn = document.querySelector('.edit-profile-btn');
    if (editProfileBtn) {
        editProfileBtn.addEventListener('click', function() {
            const editProfileModal = new bootstrap.Modal(document.getElementById('edit-profile-modal'));
            editProfileModal.show();
        });
    }
    
    // 프로필 이미지 미리보기
    const profileUpload = document.getElementById('profile-upload');
    if (profileUpload) {
        profileUpload.addEventListener('change', function() {
            const file = this.files[0];
            if (file) {
                const reader = new FileReader();
                reader.onload = function(e) {
                    document.getElementById('edit-profile-image').src = e.target.result;
                };
                reader.readAsDataURL(file);
            }
        });
    }
    
    // 비밀번호 토글 기능
    setupPasswordToggle('toggle-edit-password', 'edit-password', 'eye-edit-icon');
    setupPasswordToggle('toggle-confirm-edit-password', 'confirm-edit-password', 'eye-confirm-edit-icon');
    
    // 프로필 수정 폼 제출
    const editProfileForm = document.getElementById('edit-profile-form');
    if (editProfileForm) {
        editProfileForm.addEventListener('submit', function(e) {
            e.preventDefault();
            
            // 비밀번호 유효성 검사
            const password = document.getElementById('edit-password').value;
            const confirmPassword = document.getElementById('confirm-edit-password').value;
            
            if (password && password !== confirmPassword) {
                showAlert('비밀번호가 일치하지 않습니다.', 'danger');
                return;
            }
            
            // 폼 데이터 생성
            const formData = new FormData(editProfileForm);
            
            // API 호출
            fetch('/api/users/profile', {
                method: 'POST',
                body: formData,
                // 로그인한 사용자의 인증 정보가 자동으로 포함됨
            })
            .then(response => {
                if (!response.ok) {
                    throw new Error('프로필 업데이트에 실패했습니다.');
                }
                return response.json();
            })
            .then(data => {
                showAlert('프로필이 성공적으로 업데이트되었습니다.', 'success');
                // 모달 닫기
                const editProfileModal = bootstrap.Modal.getInstance(document.getElementById('edit-profile-modal'));
                editProfileModal.hide();
                
                // 페이지 새로고침하여 업데이트된 정보 표시
                setTimeout(() => {
                    window.location.reload();
                }, 1500);
            })
            .catch(error => {
                console.error('Error:', error);
                showAlert(error.message, 'danger');
            });
        });
    }
    
    // 이름 상세 보기 버튼 클릭
    const viewNameBtns = document.querySelectorAll('.view-name-btn');
    viewNameBtns.forEach(btn => {
        btn.addEventListener('click', function() {
            const nameId = this.getAttribute('data-id');
            loadNameDetails(nameId);
        });
    });
    
    // 이름 삭제 버튼 클릭
    const deleteNameBtns = document.querySelectorAll('.delete-name-btn');
    deleteNameBtns.forEach(btn => {
        btn.addEventListener('click', function() {
            const nameId = this.getAttribute('data-id');
            confirmDeleteName(nameId);
        });
    });
});

// 비밀번호 토글 설정 함수
function setupPasswordToggle(toggleId, passwordId, eyeIconId) {
    const toggleBtn = document.getElementById(toggleId);
    const passwordInput = document.getElementById(passwordId);
    const eyeIcon = document.getElementById(eyeIconId);
    
    if (toggleBtn && passwordInput && eyeIcon) {
        toggleBtn.addEventListener('click', function() {
            if (passwordInput.type === 'password') {
                passwordInput.type = 'text';
                eyeIcon.classList.remove('fa-eye');
                eyeIcon.classList.add('fa-eye-slash');
            } else {
                passwordInput.type = 'password';
                eyeIcon.classList.remove('fa-eye-slash');
                eyeIcon.classList.add('fa-eye');
            }
        });
    }
}

// 알림 표시 함수
function showAlert(message, type) {
    const alertDiv = document.createElement('div');
    alertDiv.className = `alert alert-${type} alert-dismissible fade show`;
    alertDiv.setAttribute('role', 'alert');
    alertDiv.innerHTML = `
        ${message}
        <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
    `;
    
    // 알림을 상단에 추가
    const mainContent = document.getElementById('main-content');
    mainContent.insertBefore(alertDiv, mainContent.firstChild);
    
    // 3초 후 자동으로 알림 닫기
    setTimeout(() => {
        const bsAlert = new bootstrap.Alert(alertDiv);
        bsAlert.close();
    }, 3000);
}

// 이름 상세 정보 로드 함수
function loadNameDetails(nameId) {
    fetch(`/api/names/${nameId}`, {
        method: 'GET'
    })
    .then(response => {
        if (!response.ok) {
            throw new Error('이름 정보를 불러오는데 실패했습니다.');
        }
        return response.json();
    })
    .then(data => {
        // 모달 내용 채우기
        document.getElementById('viewNameModalLabel').textContent = `이름: ${data.nameValue}`;
        document.getElementById('detail-name-value').textContent = data.nameValue;
        document.getElementById('detail-created-date').textContent = formatDate(data.createdAt);
        document.getElementById('detail-name-meaning').textContent = data.meaning || '의미 정보가 없습니다.';
        document.getElementById('detail-name-notes').textContent = data.notes || '메모가 없습니다.';
        
        // 글자별 의미 채우기
        const charactersContainer = document.getElementById('detail-characters-container');
        charactersContainer.innerHTML = '';
        
        if (data.characters && data.characters.length > 0) {
            data.characters.forEach(char => {
                const charDiv = document.createElement('div');
                charDiv.className = 'character-item';
                charDiv.innerHTML = `
                    <h5>${char.character}</h5>
                    <p>${char.meaning || '의미 정보가 없습니다.'}</p>
                `;
                charactersContainer.appendChild(charDiv);
            });
        } else {
            charactersContainer.innerHTML = '<p>글자별 의미 정보가 없습니다.</p>';
        }
        
        // 수정 링크 설정
        document.getElementById('detail-edit-link').href = `/generator?edit=${nameId}`;
        
        // 모달 표시
        const viewNameModal = new bootstrap.Modal(document.getElementById('view-name-modal'));
        viewNameModal.show();
    })
    .catch(error => {
        console.error('Error:', error);
        showAlert(error.message, 'danger');
    });
}

// 이름 삭제 확인 함수
function confirmDeleteName(nameId) {
    if (confirm('정말로 이 이름을 삭제하시겠습니까? 이 작업은 되돌릴 수 없습니다.')) {
        fetch(`/api/names/${nameId}`, {
            method: 'DELETE',
            headers: {
                'Content-Type': 'application/json',
                // CSRF 토큰이 필요한 경우 여기에 추가
                'X-CSRF-TOKEN': document.querySelector('meta[name="_csrf"]').getAttribute('content')
            }
        })
        .then(response => {
            if (!response.ok) {
                throw new Error('이름 삭제에 실패했습니다.');
            }
            return response.json();
        })
        .then(data => {
            showAlert('이름이 성공적으로 삭제되었습니다.', 'success');
            
            // 삭제된 행 제거
            const row = document.querySelector(`.delete-name-btn[data-id="${nameId}"]`).closest('tr');
            row.remove();
            
            // 페이지에 더 이상 이름이 없으면 빈 상태 표시
            const tbody = document.querySelector('.names-list tbody');
            if (tbody && tbody.children.length === 0) {
                const namesList = document.querySelector('.names-list');
                const emptyState = document.createElement('div');
                emptyState.className = 'empty-state';
                emptyState.innerHTML = `
                    <div class="empty-icon">
                        <i class="fas fa-file-alt fa-3x"></i>
                    </div>
                    <p>아직 생성한 이름이 없습니다.</p>
                    <a href="/generator" class="btn btn-primary">이름 만들기</a>
                `;
                namesList.parentNode.replaceChild(emptyState, namesList);
            }
        })
        .catch(error => {
            console.error('Error:', error);
            showAlert(error.message, 'danger');
        });
    }
}

// 날짜 포맷팅 함수
function formatDate(dateString) {
    const date = new Date(dateString);
    return date.toLocaleDateString('ko-KR', { 
        year: 'numeric',