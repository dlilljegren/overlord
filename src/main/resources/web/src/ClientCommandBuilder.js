{
	function addUnitAt(col,row){
	    const obj = {};
	    obj.cord = {};
	    obj.cord.col = col;
	    obj.cord.row = row;
	    obj.cmd ="add";
	    obj.p = "a";
	    return obj;
	}

	function makeLoginMsg(cookie){
	    const obj = {};
	    obj.userCookie = cookie;
	    obj.p='l';
	    return obj;
	}

	function goToSection(sectionNo){
	    const obj ={};
	    obj.sectionNo = sectionNo;
	    obj.p ='s';
	    return obj;
	}
}