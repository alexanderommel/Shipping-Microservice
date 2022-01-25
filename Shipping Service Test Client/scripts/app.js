async function fetchJwt(username_){
    try{        
        const response = await fetch("http://localhost:8088/drivers/oauth",
        {
            credentials:'include',
            method:'POST',
            headers: {'Content-Type':'application/json','Accept':'application/json'},
            body: JSON.stringify({username:username_})
        });
        console.log(response)
        if(response.status === 200){
            console.log('Sucessful authentication')
            const data = await response.json();
            const jwt = data.jwt;
            console.log('JWT: {',jwt,'}');
            return jwt;
        }else{
            console.log('Authentication failed')
        }
    }catch(error){
        console.log(error);
    }
    console.log('returning null')
    return null;
}

const homeUrl = "http://localhost:8081/shippingtest/home.html";
const jwtAuthenticationUrl = "http://localhost:8088/friendly/jwt";

async function jwtAuthentication(jwt_){
    //var jwt_ = "Bearer a0120x"
    console.log('JWTT Validation...')
    try{
        console.log(jwt_);
        const response = await fetch("http://localhost:8088/drivers/jwt",{
            credentials:'include',
            method:'POST',
            headers: {'Content-Type':'application/json', 'Accept':'application/json','Authorization':jwt_}
        });
        console.log(response)
        if(response.status === 200){
            console.log('Successful token validation')
            return true;
        }else{
            console.log('JWT Validation failed');
        }
    }catch(error){
        console.log(error);
    }
    return false;
}

async function authenticate(){
    console.log('Authenticating user...')
    var username = document.login.username.value
    var password = document.login.password.value
    const validParams = validateAuthenticationParameters(username,password)
    if (validParams===true){
        sessionStorage.setItem('username',username);
        console.log('Sending HttpRequest to Shipping Service')
        response = await fetchJwt(username);
        if(response === null){
            var status_p = document.getElementById('authentication_status_p')
            var content = document.createTextNode('Bad credentials!')
            status_p.appendChild(content);
        }else{
            var status_p = document.getElementById('authentication_status_p')
            status_p.innerHTML='';
            sessionStorage.setItem('jwt',response);
            validation_response = await jwtAuthentication(response);
            document.location.href = homeUrl;
        }
    }
}

function validateAuthenticationParameters(username,password){
    return true;
}