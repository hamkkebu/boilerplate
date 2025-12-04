<#import "template.ftl" as layout>
<@layout.registrationLayout displayMessage=!messagesPerField.existsError('firstName','lastName','email','username','password','password-confirm'); section>
    <#if section = "header">
    <#elseif section = "form">
    <div id="kc-form">
      <div id="kc-form-wrapper">
        <div class="login-title">
            <h1>Create Account</h1>
            <p>새 계정을 만들어 시작하세요</p>
        </div>
        <form id="kc-register-form" action="${url.registrationAction}" method="post">
            <div class="form-group">
                <label for="firstName" class="control-label">${msg("firstName")}</label>
                <input type="text" id="firstName" class="form-control" name="firstName"
                       value="${(register.formData.firstName!'')}"
                       placeholder="이름을 입력하세요"
                       aria-invalid="<#if messagesPerField.existsError('firstName')>true</#if>"
                />
                <#if messagesPerField.existsError('firstName')>
                    <span class="error-text" aria-live="polite">
                        ${kcSanitize(messagesPerField.get('firstName'))?no_esc}
                    </span>
                </#if>
            </div>

            <div class="form-group">
                <label for="lastName" class="control-label">${msg("lastName")}</label>
                <input type="text" id="lastName" class="form-control" name="lastName"
                       value="${(register.formData.lastName!'')}"
                       placeholder="성을 입력하세요"
                       aria-invalid="<#if messagesPerField.existsError('lastName')>true</#if>"
                />
                <#if messagesPerField.existsError('lastName')>
                    <span class="error-text" aria-live="polite">
                        ${kcSanitize(messagesPerField.get('lastName'))?no_esc}
                    </span>
                </#if>
            </div>

            <div class="form-group">
                <label for="email" class="control-label">${msg("email")}</label>
                <input type="email" id="email" class="form-control" name="email"
                       value="${(register.formData.email!'')}"
                       placeholder="이메일을 입력하세요"
                       autocomplete="email"
                       aria-invalid="<#if messagesPerField.existsError('email')>true</#if>"
                />
                <#if messagesPerField.existsError('email')>
                    <span class="error-text" aria-live="polite">
                        ${kcSanitize(messagesPerField.get('email'))?no_esc}
                    </span>
                </#if>
            </div>

            <#if !realm.registrationEmailAsUsername>
            <div class="form-group">
                <label for="username" class="control-label">${msg("username")}</label>
                <input type="text" id="username" class="form-control" name="username"
                       value="${(register.formData.username!'')}"
                       placeholder="아이디를 입력하세요"
                       autocomplete="username"
                       aria-invalid="<#if messagesPerField.existsError('username')>true</#if>"
                />
                <#if messagesPerField.existsError('username')>
                    <span class="error-text" aria-live="polite">
                        ${kcSanitize(messagesPerField.get('username'))?no_esc}
                    </span>
                </#if>
            </div>
            </#if>

            <#if passwordRequired??>
            <div class="form-group">
                <label for="password" class="control-label">${msg("password")}</label>
                <div class="password-wrapper">
                    <input type="password" id="password" class="form-control" name="password"
                           placeholder="비밀번호를 입력하세요"
                           autocomplete="new-password"
                           aria-invalid="<#if messagesPerField.existsError('password','password-confirm')>true</#if>"
                    />
                    <button type="button" class="password-toggle" onclick="togglePassword('password')" aria-label="Toggle password visibility">
                        <svg id="eye-icon-password" xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                            <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"></path>
                            <circle cx="12" cy="12" r="3"></circle>
                        </svg>
                    </button>
                </div>
                <#if messagesPerField.existsError('password')>
                    <span class="error-text" aria-live="polite">
                        ${kcSanitize(messagesPerField.get('password'))?no_esc}
                    </span>
                </#if>
            </div>

            <div class="form-group">
                <label for="password-confirm" class="control-label">${msg("passwordConfirm")}</label>
                <div class="password-wrapper">
                    <input type="password" id="password-confirm" class="form-control" name="password-confirm"
                           placeholder="비밀번호를 다시 입력하세요"
                           autocomplete="new-password"
                           aria-invalid="<#if messagesPerField.existsError('password-confirm')>true</#if>"
                    />
                    <button type="button" class="password-toggle" onclick="togglePassword('password-confirm')" aria-label="Toggle password visibility">
                        <svg id="eye-icon-password-confirm" xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                            <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"></path>
                            <circle cx="12" cy="12" r="3"></circle>
                        </svg>
                    </button>
                </div>
                <#if messagesPerField.existsError('password-confirm')>
                    <span class="error-text" aria-live="polite">
                        ${kcSanitize(messagesPerField.get('password-confirm'))?no_esc}
                    </span>
                </#if>
            </div>
            </#if>

            <#if recaptchaRequired??>
            <div class="form-group">
                <div class="g-recaptcha" data-size="compact" data-sitekey="${recaptchaSiteKey}"></div>
            </div>
            </#if>

            <div id="kc-form-buttons">
                <input class="btn btn-primary btn-block btn-lg" type="submit" value="${msg("doRegister")}"/>
            </div>
        </form>
        </div>
    </div>
    <script>
    function togglePassword(inputId) {
        var pwd = document.getElementById(inputId);
        var icon = document.getElementById('eye-icon-' + inputId);
        if (pwd.type === 'password') {
            pwd.type = 'text';
            icon.innerHTML = '<path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"></path><line x1="1" y1="1" x2="23" y2="23"></line>';
        } else {
            pwd.type = 'password';
            icon.innerHTML = '<path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"></path><circle cx="12" cy="12" r="3"></circle>';
        }
    }
    </script>
    <#elseif section = "info" >
        <div id="kc-registration">
            <span>${msg("backToLogin")} </span>
            <a href="${url.loginUrl}">${msg("doLogIn")}</a>
        </div>
    </#if>
</@layout.registrationLayout>
