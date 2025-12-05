// Tab switching functionality
function showTab(tabName) {
    // Hide all tab contents
    document.querySelectorAll('.tab-content').forEach(tab => {
        tab.classList.remove('active');
    });
    
    // Deactivate all tab buttons
    document.querySelectorAll('.tab-button').forEach(button => {
        button.classList.remove('active');
    });
    
    // Show the selected tab content
    document.getElementById(tabName).classList.add('active');
    
    // Activate the clicked tab button
    event.currentTarget.classList.add('active');
}

// Handle login form submission
async function handleLogin(event) {
    event.preventDefault();
    
    const username = document.getElementById('loginUsername').value.trim();
    const password = document.getElementById('loginPassword').value;
    const messageDiv = document.getElementById('loginMessage');
    
    try {
        const response = await fetch('/api/auth/login', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({ username, password })
        });
        
        const data = await response.json();
        
        if (response.ok) {
            // Store user data in sessionStorage
            sessionStorage.setItem('user', JSON.stringify(data.user));
            
            // Redirect based on user role
            if (data.user.role === 'ROLE_ADMIN') {
                window.location.href = '/admin/';
            } else {
                window.location.href = '/store.html';
            }
        } else {
            showMessage(messageDiv, data.message || 'Login failed', 'error');
        }
    } catch (error) {
        console.error('Login error:', error);
        showMessage(messageDiv, 'An error occurred during login', 'error');
    }
}

// Handle signup form submission
async function handleSignup(event) {
    event.preventDefault();
    
    const userData = {
        username: document.getElementById('signupUsername').value.trim(),
        email: document.getElementById('signupEmail').value.trim(),
        password: document.getElementById('signupPassword').value,
        firstName: document.getElementById('signupFirstName').value.trim(),
        lastName: document.getElementById('signupLastName').value.trim(),
        phoneNumber: document.getElementById('signupPhone').value.trim(),
        address: document.getElementById('signupAddress').value.trim()
    };
    
    const messageDiv = document.getElementById('signupMessage');
    
    try {
        const response = await fetch('/api/auth/signup', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(userData)
        });
        
        const data = await response.json();
        
        if (response.ok) {
            showMessage(messageDiv, 'Registration successful! Please login.', 'success');
            // Clear form
            event.target.reset();
            // Switch to login tab
            showTab('login');
        } else {
            showMessage(messageDiv, data.message || 'Registration failed', 'error');
        }
    } catch (error) {
        console.error('Signup error:', error);
        showMessage(messageDiv, 'An error occurred during registration', 'error');
    }
}

// Helper function to show messages
function showMessage(element, message, type = 'info') {
    element.textContent = message;
    element.className = `message ${type}`;
    
    // Clear message after 5 seconds
    setTimeout(() => {
        element.textContent = '';
        element.className = 'message';
    }, 5000);
}

// Authentication is disabled
function checkAuth() {
    // Authentication is currently disabled
    return true;
}

// Check authentication status when page loads
checkAuth();
