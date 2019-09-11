public Map<String, Object> ntsPdfSendApi(Map<String, Object> param, Map<String, Object> idtl) throws Exception {
		String p_pwd = null;	//비밀 번호 없을시 null		
		StringBuilder log = new StringBuilder();
		
		String fhost = (String) idtl.get("ADDR");
		String fid = (String) idtl.get("ID");
		String fpwd = (String) idtl.get("PWD");
		String fpath = (String) idtl.get("ROOT");
		int fport = Integer.parseInt(idtl.get("PORT").toString());
		String fileName = (String) param.get("FILE_NM");
		
		Map<String, Object> result = new HashMap<String, Object>();
		
		//PDF파일 byte[]로 리턴
		byte[] pdfBytes = FtpTool.getFileAsByte(fhost, fport, fid, fpwd, FTP.BINARY_FILE_TYPE, fpath + "/" + fileName, fileName);
		System.out.println("\n\n<!-- ["+ fileName + "] -->");
		
		//PDF파일이 아닌 경우 빠져나옴
		if (!fileName.toUpperCase().endsWith(".PDF")) {
			System.out.println("<!-- PDF 파일이 아닙니다. -->");
			//return;
		}
		
		//파일 내용 읽기
		//byte[] pdfBytes = file.get()
		
		boolean isSuccess = false;
		
		/* [Step1] 전자문서 위변조 검증 */
		try {
			/* 진본성 검증 초기화 */
			DSTSPDFSig dstsPdfsig = new DSTSPDFSig();
		    
		    dstsPdfsig.init(pdfBytes);
		    dstsPdfsig.tokenParse();
		    
		    /* 전자문서 검증 */
		    isSuccess = dstsPdfsig.tokenVerify();
		    
		    if( isSuccess ) {
		    	log = new StringBuilder();
		    	log.append("################################ NTF PDF FILE Electronic document Verification ##################################");
		    	log.append("\n## Result :: OK");
		    	logger.debug(log);
		    	
		    } else {
		    	String msg = dstsPdfsig.getTstVerifyFailInfo();
		    	log = new StringBuilder();
		    	log.append("################################ NTF PDF FILE Electronic document Verification ##################################");
		    	log.append("\n## Result :: Fail");
		    	log.append("\n## error Msg :: " + msg);
		    	logger.debug(log);
		    	//return;
		    }
		    
		  //기다 검증결과 Exception
		} catch (DVException e) {
			//code :: 2100
			//message :: PDF 문서내에 ByteRange정보가 없거나 잘못되었습니다.
			log = new StringBuilder();
	    	log.append("################################ Error PDF File ##################################");
	    	log.append("\n## Result :: Fail");
	    	log.append("\n## Error Code :: 2100");
	    	log.append("\n## ErrorFile :: " + fileName);
	    	logger.debug(log);
			
			result.put("ErrorCode", "2100");
			result.put("ErrorMsg", e.getMessage());
			result.put("ErrorFile", fileName);
		}
		
		/* [Step2] XML(or SAM) 데이터 추출 */
		try {
			if(isSuccess) {
				ExportCustomFile pdf = new ExportCustomFile();
				
				//데이터 추출
				byte[] buf = pdf.NTS_GetFileBufEx(pdfBytes, p_pwd, "XML", false);
				int v_ret = pdf.NTS_GetLastError();
				
				if(v_ret == 1) {
					
					String strXml = new String(buf, "UTF-8");
					//정상처리
//					logger.debug("\n## strXml :: "+ strXml);
					Map<String, Object> xmap = marshal(strXml);
					logger.debug("################################ demon FTP ftpFileList ##################################");
//					logger.debug("\n## xmp :: " + xmap);
					List<Map> xlist = MarshallerUtil.getList(xmap);
//					logger.debug("\n## xlist :: " + xlist);

				} else if(v_ret == 0) {
					//logger.debug("\n## v_ret == 0 <!-- 연말정산간소화 표준 전자문서가 아닙니다. -->");
					log = new StringBuilder();
			    	log.append("################################ Not NTS standard Electronic document ##################################");
			    	log.append("\n## Result :: Fail");
			    	log.append("\n## Error Code :: 0");
			    	log.append("\n## ErrorFile :: " + fileName);
			    	logger.debug(log);

				} else if(v_ret == -1) {
					//logger.debug("\n## v_ret == -1 <!-- 비밀번호가 맞지 않습니다. -->");
					log.append("################################ Not NTS standard Electronic Password ##################################");
			    	log.append("\n## Result :: Fail");
			    	log.append("\n## Error Code :: -1");
			    	log.append("\n## ErrorFile :: " + fileName);
			    	logger.debug(log);
			    	
				} else if(v_ret == -2) {
					//logger.debug("\n## v_ret == -2 <!-- PDF문서가 아니거나 손상된 문서입니다. -->");
					log.append("################################ Not PDF file or Damaged File ##################################");
			    	log.append("\n## Result :: Fail");
			    	log.append("\n## Error Code :: -2");
			    	log.append("\n## ErrorFile :: " + fileName);
			    	logger.debug(log);
			    	
				} else {
					//logger.debug("\n## v_ret == -3 <!-- 데이터 추출에 실패하였습니다. -->");
					log.append("################################ Fail Data exraction  ##################################");
			    	log.append("\n## Result :: Fail");
			    	log.append("\n## Error Code :: -3");
			    	log.append("\n## ErrorFile :: " + fileName);
			    	logger.debug(log);
				}
			}
		} catch(Exception e) {
			logger.debug("\n## Exception :: " + e.getMessage());
			log.append("################################ System Exception  ##################################");
	    	log.append("\n## Result :: Fail");
	    	log.append("\n## ErrorCode :: " + e.hashCode());
	    	log.append("\n## ErrorMsg :: " + e.getMessage());
	    	logger.debug(log);
	    	e.printStackTrace();
		}
		return result;
	}
