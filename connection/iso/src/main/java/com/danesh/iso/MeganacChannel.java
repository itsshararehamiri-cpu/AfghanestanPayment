package com.danesh.iso;

import android.util.Log;

import java.io.IOException;
import java.net.ServerSocket;

import org.jpos.iso.BaseChannel;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOPackager;
import org.jpos.iso.ISOUtil;
import org.jpos.util.LogEvent;
import org.jpos.util.Logger;

//import com.tosantechno.paymentacquirerlib.sadad.core.ByteUtil;
//import com.tosantechno.paymentacquirerlib.sadad.core.ISOExceptionUtil;

public class MeganacChannel extends BaseChannel {

		public MeganacChannel() {
			super();
		}

		/**
		 * Construct client ISOChannel
		 *
		 * @param host
		 *            server TCP Address
		 * @param port
		 *            server port number
		 * @param p
		 *            an ISOPackager (should be ISO87BPackager)
		 * @see org.jpos.iso.packager.ISO87BPackager
		 */
		public MeganacChannel(String host, int port, ISOPackager p) {
			super(host, port, p);
		}

		/**
		 * Construct server ISOChannel
		 *
		 * @param p
		 *            an ISOPackager (should be ISO87BPackager)
		 * @exception IOException
		 * @see org.jpos.iso.packager.ISO87BPackager
		 */
		public MeganacChannel(ISOPackager p) throws IOException {
			super(p);
		}

		/**
		 * constructs a server ISOChannel associated with a Server Socket
		 *
		 * @param p
		 *            an ISOPackager
		 * @param serverSocket
		 *            where to accept a connection
		 * @exception IOException
		 * @see ISOPackager
		 */
		public MeganacChannel(ISOPackager p, ServerSocket serverSocket)
				throws IOException {
			super(p, serverSocket);
		}



	@Override
	protected void sendMessage(byte[] b, int offset, int len) throws IOException {

	//	System.out.println("send message="+ ISOUtil.hexString(b,offset,len));

//	Log.e("send message=", ISOUtil.hexString(b, offset, len));
		super.sendMessage(b, offset, len);
	}

	@Override
	protected byte[] streamReceive() throws IOException {
		int count = serverIn.available();

		// create buffer
		byte[] bs = new byte[count];

		// read data into buffer
		serverIn.read(bs);
		//System.out.println("Receive message="+ ISOUtil.hexString(bs));
		//Log.e("Receive message=",ISOUtil.hexString(bs));
		return bs;

	}



		protected void sendMessageLength(int len) throws IOException {
//		if(logger.isDebugEnabled())
//			logger.debug("send message len with len= "+len);
//		serverOut.write(len >> 8);
//		serverOut.write(len);
		}
		@Override
		protected int getHeaderLength() {
			return 5;
		}
		@Override
		protected void sendMessageHeader(ISOMsg m, int len) throws IOException {
			byte[] h = m.getHeader();
			if(h == null)
				throw new IOException("send header fail because header is null");
			if(h.length !=5)
				throw new IOException("send header fail because header length is not valid len="+h.length);
			if (h != null) {
				// swap src/dest address
				byte[] tmp = new byte[2];
				System.arraycopy (h,   1, tmp, 0, 2);
				System.arraycopy (h,   3,   h, 1, 2);
				System.arraycopy (tmp, 0,   h, 3, 2);
			}
			if (h != null)
				serverOut.write(h);
//		if(logger.isDebugEnabled())
//			logger.debug("send message header that byte array is "+ com.tosan.bpm.framework.util.ByteUtil.convert2HexString(m.getHeader()));
		}
		public void setHeader (String header) {
			super.setHeader (ISOUtil.str2bcd(header, false));
		}
		@Override
		protected byte[] readHeader(int hLen) throws IOException
		{
			byte[] header =  super.readHeader(hLen);
//		if(logger.isDebugEnabled())
//			logger.debug("receive message header  is "+ com.tosan.bpm.framework.util.ByteUtil.convert2HexString(header));
			return header;

		}

//		protected int getMessageLength() throws IOException, ISOException {
//		int l = 0;
//		byte[] b = new byte[2];
//		// ignore polls (0 message length)
//		while (l == 0) {
//			serverIn.readFully(b, 0, 2);
//
//			Log.e("get len", ByteUtil.convert2HexString(b));
//
////			if(logger.isDebugEnabled())
////				logger.debug("recive message len that byte array is "+ com.tosan.bpm.framework.util.ByteUtil.convert2HexString(b));
//			l = ((((int) b[0]) & 0xFF) << 8) | (((int) b[1]) & 0xFF);
//			if (l == 0) {
//				serverOut.write(b);
//				serverOut.flush();
//				Logger.log(new LogEvent(this, "poll"));
//			}
//		}
////		if(logger.isDebugEnabled())
////			logger.debug("recive message len t is "+ l);
//
//		return l;
//		}
		@Override
		public ISOMsg receive() throws IOException, ISOException {
			try {

				ISOMsg bs=	super.receive();
				Log.e("Receive message=",ISOUtil.hexString(bs.pack()));
				return bs;
			} catch (ISOException e)
			{
				if (ISOExceptionUtil.isIOException(e))
					throw new IOException(e);
				throw e;
			}
		}

		@Override
		public void send(byte[] b) throws IOException, ISOException {
			try {
				super.send(b);
			} catch (ISOException e)
			{
				if (ISOExceptionUtil.isIOException(e))
					throw new IOException(e);
				throw e;
			}

		}

		@Override
		public void send(ISOMsg m) throws IOException, ISOException {
			// TODO Auto-generated method stub
			try {
				super.send(m);
			} catch (ISOException e)
			{
				if (ISOExceptionUtil.isIOException(e))
					throw new IOException(e);
				throw e;
			}

		}
	
}
