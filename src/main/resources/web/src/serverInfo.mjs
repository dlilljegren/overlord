const server = document.getElementById("message_label");


let lastTimeout;
export function setInfoMessage(text){
    server.textContent = text;
    clearTimeout(lastTimeout);
    lastTimeout = setTimeout(clear, 3000);               
}

function clear(){
    server.textContent = "";
}