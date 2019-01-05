//Not yet supported by browsers import WsClient from './wsClient.mjs';

self.importScripts('wsClient.mjs');


let session;

const callback = d=>{
	self.postMessage(d);
}

callback(["WebWorkerSayHello"])

/**
 * From main thread to worker thread
 */
onmessage = function(e){
	console.log("Game script sent:"+JSON.stringify(e.data));



	

	if(e.data.cmd == "connect"){
		var connectInfo = e.data.connectInfo;
		session = new WsClient(connectInfo.wsUri, callback );
        session.connect();
	}
	else if(e.data.cmd=="close"){
	    if(session)session.close();
	    this.close();
	}
	else if(!session){
		console.error("Session is null");
	}
	else{		
		session.sendMessageToServer(e.data);
	}
};


//Create the client on start-up


