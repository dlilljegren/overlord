const form1 = document.getElementsByClassName("cbox");

const enterListeners = [];

for(var i=0;i<form1.length;i++){
    const checked = form1[i];

    const add = checked.parentElement.getElementsByClassName("add")[0];
    const message = checked.parentElement.getElementsByClassName("message")[0];
    
    checked.addEventListener("click",()=>{
        if (checked.checked) {
            add.innerText="Hit Enter to Submit";
            message.focus();
        }
        else{
            message.value="";
            add.innerText="Register";
        }
    });

    message.addEventListener("keypress",(e)=>{
        // look for window.event in case event isn't passed in
        e = e || window.event;
        if (e.keyCode == 13)
        {
            console.info("Enter Pressed");
            notifyEnterListeners(message.value);
            return false;
        }
        return true;
    });
}

export function addEnterListener(listener){
    enterListeners.push(listener);
}

function notifyEnterListeners(messageElement){
    for(var l of enterListeners){
        l(messageElement);
    }
}

