function login(){
    var request = new XMLHttpRequest();
}

const fetchJwt = async() => {
    try{
        const headerss = {'Access-Control-Allow-Origin':'*','Content-Type':'application/json'}
        const response = await fetch("http://localhost:8088/greeting",
        {
            method:'POST',
            
            body:JSON.stringify({username:'bunny'})
        });
        console.log(response)
        if(response.status === 200){
            const data = await response.json();
            console.log(data);
        }else{
            console.log('HttpStatus inaceptable')
        }
    }catch(error){
        console.log(error);
    }
}

fetchJwt();