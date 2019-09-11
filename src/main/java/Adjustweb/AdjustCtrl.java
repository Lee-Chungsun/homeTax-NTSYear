public void ntsYearEndAdjust(HttpServletRequest request, HttpServletResponse response) throws Exception {
	StringBuilder log = new StringBuilder();
	log.append("\n################################ revenue ntsYearEndAdjust Start ################################");
	log.append("\n### ntsYearEndAdjust Force Recv ");
	logger.debug(log);
		
	JSONParser jsonParser = new JSONParser();
	JSONObject jsonObject = (JSONObject) jsonParser.parse(readBody(request));
	
	  
    	Set<String> files = ftpFileList(param);
    	for(String file : files){
		param.put("FILE_NM", file);
	    	if(file.equals(empId)){
			result = ntsPdfSendApi(param);
		}
	}
}
