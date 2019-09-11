/**
	 * FTP 서버로부터 파일을 읽어 byte[] 로 반환하는 기능
	 * 
	 * @param String
	 *            ftp_ip 아이피
	 * @param int
	 *            ftp_port 포트
	 * @param String
	 *            ftp_id 아이디
	 * @param String
	 *            ftp_pw 비밀번호
	 * @param int
	 *            ftp_mode 전송모드(ASCII, BINARY)
	 * @param String
	 *            remote 수신할 파일명
	 * @param String
	 *            local 수신받을 파일명
	 * @return byte[] buffer 실제파일데이터
	 * @exception Exception
	 */
	public static byte[] getFileAsByte(String ftp_ip, int ftp_port, String ftp_id, String ftp_pw, int ftp_mode,String remote, String local) throws Exception {
		// 수신결과
		byte[] outByte = null;
		boolean result = false;

		FTPClient ftpClient = null;
		ByteArrayOutputStream out = null;

		try {

			// 1. 연결시작
			ftpClient = new FTPClient();
			result = connect(ftpClient, ftp_ip, ftp_port, ftp_id, ftp_pw, ftp_mode);
			if (!result) {
				return outByte;
			}

			// 2. FTP 서버의 작업할 디렉토리로 이동
			String[] ser_path = splitPathAndName(remote, "/");
			String path = ser_path[0];
			String name = ser_path[1];
			result = ftpClient.changeWorkingDirectory(path);
			if (!result) {
				return outByte;
			}

			// 3. 파일 수신 수행
			out = new ByteArrayOutputStream();

			result = ftpClient.retrieveFile(name, out);
			if (!result) {
				return outByte;
			}

			// 4. byte[] 결과
			if (out != null) {
				outByte = out.toByteArray();
			}

		} finally {
			try {
				disconnect(ftpClient);
			} catch (Exception ignore) {
			}
			close(out);
		}
		return outByte;
	}
