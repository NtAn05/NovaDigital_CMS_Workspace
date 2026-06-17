document.addEventListener('DOMContentLoaded', function () {
    'use strict';

    // Lấy form liên hệ cần validate
    const contactForm = document.getElementById('contactForm');

    if (contactForm) {
        contactForm.addEventListener('submit', function (event) {
            // Nếu form không vượt qua bài test kiểm tra dữ liệu của trình duyệt
            if (!contactForm.checkValidity()) {
                event.preventDefault();
                event.stopPropagation();
            } else {
                // Nếu dữ liệu đã chuẩn (Functional Form)
                event.preventDefault(); // Chặn hành vi load lại trang thật
                
                // Thu thập thử dữ liệu
                const name = document.getElementById('fullName').value;
                
                // Hiển thị thông báo Alert tương tác thành công theo yêu cầu bài
                alert(`Cảm ơn ${name}! NovaDigital đã nhận được yêu cầu liên hệ của nhóm bạn thành công.`);
                
                contactForm.reset(); // Xóa sạch form sau khi gửi thành công
                contactForm.classList.remove('was-validated');
                return;
            }

            contactForm.classList.add('was-validated');
        }, false);
    }
});