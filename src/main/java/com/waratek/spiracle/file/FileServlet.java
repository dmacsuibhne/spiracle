/*
 *  Copyright 2014 Waratek Ltd.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.waratek.spiracle.file;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

/**
 * Servlet implementation class FileServlet
 */
@WebServlet("/FileServlet")
public class FileServlet extends HttpServlet {
	private static final Logger logger = Logger.getLogger(FileServlet.class);
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public FileServlet() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		executeRequest(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		executeRequest(request, response);
	}

	private void executeRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
		final String urlHttp = "http://www.google.com/";
		final String urlHttps = "https://www.google.com/";
		logger.info("DEBUG: Making HTTP GET request at start of FileServlet request handling");
		basicGet(urlHttp);
		logger.info("DEBUG: Making HTTPS GET request at start of FileServlet request handling");
		basicGet(urlHttps);
		logger.info("DEBUG: Making HTTP POST request at start of FileServlet request handling");
		basicPost(urlHttp);
		logger.info("DEBUG: Making HTTPS POST request at start of FileServlet request handling");
		basicPost(urlHttps);
		HttpSession session = request.getSession();

		String method = request.getParameter("fileArg");
		String path = request.getParameter("filePath");
		String textData = request.getParameter("fileText");

		if(method.equals("read")) {
			read(session, path);
		} else if(method.equals("write")) {
			write(session, path, textData);
		} else if(method.equals("delete")) {
			delete(session, path);
		}

		logger.info(method + " " + path + " " + textData);

		response.sendRedirect("file.jsp");
	}

	private void basicGet(String url) throws IOException
	{
		URL obj = new URL(url);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();
		con.setRequestMethod("GET");
		logger.info("DEBUG: Response code: " + con.getResponseCode());
		printBasicResponseBody(con.getInputStream());
	}

	private void basicPost(String url) throws IOException
	{
		URL obj = new URL(url);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();
		con.setRequestMethod("POST");

		// For POST only - START
		con.setDoOutput(true);
		OutputStream os = con.getOutputStream();
		os.write("userName=Donn".getBytes());
		os.flush();
		os.close();
		// For POST only - END

		logger.info("DEBUG: Response status: " + con.getHeaderField(null)); //getting this way avoid exception for status code >400
		printBasicResponseBody(con.getErrorStream()); // Assumes post is going to fail because I'm sending a silly request
	}

	private void printBasicResponseBody(InputStream inputStream) throws IOException
	{
		BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
		String inputLine;
		StringBuilder response = new StringBuilder();
		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();
		logger.info("DEBUG: Response body: " + response);
	}

	private void delete(HttpSession session, String path) {
		File f = new File(path);
		f.delete();
		session.setAttribute("fileContents", "");
	}

	private void read(HttpSession session, String path) {
		session.setAttribute("fileContents", readFile(path));
	}

	private void write(HttpSession session, String path, String textData)
			throws IOException {
		File f = new File(path);
		FileWriter fw = new FileWriter(f);
		BufferedWriter bw = new BufferedWriter(fw);
		bw.write(textData);
		bw.close();
		fw.close();

		read(session, path);
	}

	private String readFile(String pathname) {
		try {
			File file = new File(pathname);
			StringBuilder fileContents = new StringBuilder((int)file.length());
			Scanner scanner = new Scanner(file);
			String lineSeparator = System.getProperty("line.separator");

			try {
				while(scanner.hasNextLine()) {
					fileContents.append(scanner.nextLine() + lineSeparator);
				}
				return fileContents.toString();
			} finally {
				scanner.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
			return e.getMessage();
		}
	}
}
