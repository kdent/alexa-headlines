package com.seaglass.alexa;

public class DialogManager {


	public static Response nextTurn() {
		Response resp = new Response();
		resp.setSurfaceText("I am responding to your turn.");
		return resp;
	}


}
