/*
 * Copyright (c) 2002-2004 LWJGL Project
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * * Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *
 * * Neither the name of 'LWJGL' nor the names of
 *   its contributors may be used to endorse or promote products derived
 *   from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.lwjgl.opengl;

/**
 * This is the Display implementation interface. Display delegates
 * to implementors of this interface. There is one DisplayImplementation
 * for each supported platform.
 * @author elias_naur
 */

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import org.lwjgl.Sys;
import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;

final class LinuxDisplay implements DisplayImplementation {
	private static final int CURSOR_HANDLE_SIZE = 8;
//	private static final int PBUFFER_HANDLE_SIZE = 24;
	private static final int NUM_BUTTONS = 3;

	private static PeerInfo peer_info;

	/* Since Xlib is not guaranteed to be thread safe, we need a way to synchronize LWJGL
	 * Xlib calls with AWT Xlib calls. Fortunately, JAWT implements LockAWT and UnlockAWT() to
	 * do just that.
	 */
	static native void lockAWT();
	static native void unlockAWT();

	/**
	 * increment and decrement display usage.
	 */
	static native void incDisplay() throws LWJGLException;
	static native void decDisplay();

	public void createWindow(DisplayMode mode, boolean fullscreen, int x, int y) throws LWJGLException {
		lockAWT();
		try {
			ByteBuffer handle = peer_info.lockAndGetHandle();
			try {
				nCreateWindow(handle, mode, fullscreen, x, y);
			} finally {
				peer_info.unlock();
			}
		} finally {
			unlockAWT();
		}
	}
	private static native void nCreateWindow(ByteBuffer peer_info_handle, DisplayMode mode, boolean fullscreen, int x, int y) throws LWJGLException;

	public void destroyWindow() {
		lockAWT();
		nDestroyWindow();
		unlockAWT();
	}
	private static native void nDestroyWindow();

	public void switchDisplayMode(DisplayMode mode) throws LWJGLException {
		lockAWT();
		try {
			nSwitchDisplayMode(mode);
		} finally {
			unlockAWT();
		}
	}
	private static native void nSwitchDisplayMode(DisplayMode mode) throws LWJGLException;

	public void resetDisplayMode() {
		lockAWT();
		try {
			nResetDisplayMode();
		} finally {
			unlockAWT();
		}
	}
	private static native void nResetDisplayMode();

	public int getGammaRampLength() {
		lockAWT();
		int length = nGetGammaRampLength();
		unlockAWT();
		return length;
	}
	private static native int nGetGammaRampLength();

	public void setGammaRamp(FloatBuffer gammaRamp) throws LWJGLException {
		lockAWT();
		try {
			nSetGammaRamp(gammaRamp);
		} finally {
			unlockAWT();
		}
	}
	private static native void nSetGammaRamp(FloatBuffer gammaRamp) throws LWJGLException;

	public String getAdapter() {
		return null;
	}

	public String getVersion() {
		return null;
	}

	public DisplayMode init() throws LWJGLException {
		lockAWT();
		try {
			DisplayMode mode = nInit();
			return mode;
		} finally {
			unlockAWT();
		}
	}
	private static native DisplayMode nInit() throws LWJGLException;

	public void setTitle(String title) {
		lockAWT();
		nSetTitle(title);
		unlockAWT();
	}
	private static native void nSetTitle(String title);

	public boolean isCloseRequested() {
		lockAWT();
		boolean result = nIsCloseRequested();
		unlockAWT();
		return result;
	}
	private static native boolean nIsCloseRequested();

	public boolean isVisible() {
		lockAWT();
		boolean result = nIsVisible();
		unlockAWT();
		return result;
	}
	private static native boolean nIsVisible();

	public boolean isActive() {
		lockAWT();
		boolean result = nIsActive();
		unlockAWT();
		return result;
	}
	private static native boolean nIsActive();

	public boolean isDirty() {
		lockAWT();
		boolean result = nIsDirty();
		unlockAWT();
		return result;
	}
	private static native boolean nIsDirty();

	public PeerInfo createPeerInfo(PixelFormat pixel_format) throws LWJGLException {
		lockAWT();
		try {
			incDisplay();
			try {
				GLContext.loadOpenGLLibrary();
				try {
					peer_info = new LinuxDisplayPeerInfo(pixel_format);
					return peer_info;
				} catch (LWJGLException e) {
					GLContext.unloadOpenGLLibrary();
					throw e;
				}
			} catch (LWJGLException e) {
				decDisplay();
				throw e;
			}
		} finally {
			unlockAWT();
		}
	}
	
	public void destroyPeerInfo() {
		lockAWT();
		peer_info = null;
		GLContext.unloadOpenGLLibrary();
		decDisplay();
		unlockAWT();
	}

	public void update() {
		lockAWT();
		nUpdate();
		unlockAWT();
	}
	private static native void nUpdate();

	public void reshape(int x, int y, int width, int height) {
		lockAWT();
		nReshape(x, y, width, height);
		unlockAWT();
	}
	private static native void nReshape(int x, int y, int width, int height);

	public DisplayMode[] getAvailableDisplayModes() throws LWJGLException {
		lockAWT();
		try {
			DisplayMode[] modes = nGetAvailableDisplayModes();
			return modes;
		} finally {
			unlockAWT();
		}
	}
	private static native DisplayMode[] nGetAvailableDisplayModes() throws LWJGLException;

	/* Mouse */
	public boolean hasWheel() {
		return true;
	}

	public int getButtonCount() {
		return NUM_BUTTONS;
	}

	public void createMouse() {
		lockAWT();
		nCreateMouse();
		unlockAWT();
	}
	private static native void nCreateMouse();
	public void destroyMouse() {
		lockAWT();
		nDestroyMouse();
		unlockAWT();
	}
	private static native void nDestroyMouse();
	
	public void pollMouse(IntBuffer coord_buffer, ByteBuffer buttons) {
		lockAWT();
		nPollMouse(coord_buffer, buttons);
		unlockAWT();
	}
	private static native void nPollMouse(IntBuffer coord_buffer, ByteBuffer buttons);
	
	public int readMouse(IntBuffer buffer, int buffer_position) {
		lockAWT();
		int count = nReadMouse(buffer, buffer_position);
		unlockAWT();
		return count;
	}
	private static native int nReadMouse(IntBuffer buffer, int buffer_position);
	
	public void grabMouse(boolean grab) {
		lockAWT();
		nGrabMouse(grab);
		unlockAWT();
	}
	private static native void nGrabMouse(boolean grab);
	
	public int getNativeCursorCapabilities() {
		lockAWT();
		try {
			incDisplay();
			int caps = nGetNativeCursorCapabilities();
			decDisplay();
			return caps;
		} catch (LWJGLException e) {
			throw new RuntimeException(e);
		} finally {
			unlockAWT();
		}
	}
	private static native int nGetNativeCursorCapabilities() throws LWJGLException;

	public void setNativeCursor(Object handle) throws LWJGLException {
		lockAWT();
		nSetNativeCursor(handle);
		unlockAWT();
	}
	private static native void nSetNativeCursor(Object handle) throws LWJGLException;
	
	public int getMinCursorSize() {
		lockAWT();
		int min_size = nGetMinCursorSize();
		unlockAWT();
		return min_size;
	}
	private static native int nGetMinCursorSize();

	public int getMaxCursorSize() {
		lockAWT();
		int max_size = nGetMaxCursorSize();
		unlockAWT();
		return max_size;
	}
	private static native int nGetMaxCursorSize();
	
	/* Keyboard */
	public void createKeyboard() throws LWJGLException {
		lockAWT();
		try {
			nCreateKeyboard();
		} finally {
			unlockAWT();
		}
	}
	private static native void nCreateKeyboard() throws LWJGLException;
	
	public void destroyKeyboard() {
		lockAWT();
		nDestroyKeyboard();
		unlockAWT();
	}
	private static native void nDestroyKeyboard();
	
	public void pollKeyboard(ByteBuffer keyDownBuffer) {
		lockAWT();
		nPollKeyboard(keyDownBuffer);
		unlockAWT();
	}
	private static native void nPollKeyboard(ByteBuffer keyDownBuffer);

	public int readKeyboard(IntBuffer buffer, int buffer_position) {
		lockAWT();
		int count = nReadKeyboard(buffer, buffer_position);
		unlockAWT();
		return count;
	}
	private static native int nReadKeyboard(IntBuffer buffer, int buffer_position);
	
	public int isStateKeySet(int key) {
		return Keyboard.STATE_UNKNOWN;
	}

	private static native void nCreateCursor(ByteBuffer handle, int width, int height, int xHotspot, int yHotspot, int numImages, IntBuffer images, int images_offset, IntBuffer delays, int delays_offset) throws LWJGLException;

	public Object createCursor(int width, int height, int xHotspot, int yHotspot, int numImages, IntBuffer images, IntBuffer delays) throws LWJGLException {
		lockAWT();
		try {
			incDisplay();
			try {
				ByteBuffer handle = BufferUtils.createByteBuffer(CURSOR_HANDLE_SIZE);
				nCreateCursor(handle, width, height, xHotspot, yHotspot, numImages, images, images.position(), delays, delays != null ? delays.position() : -1);
				return handle;
			} catch (LWJGLException e) {
				decDisplay();
				throw e;
			}
		} finally {
			unlockAWT();
		}
	}

	public void destroyCursor(Object cursorHandle) {
		lockAWT();
		nDestroyCursor(cursorHandle);
		decDisplay();
		unlockAWT();
	}
	private static native void nDestroyCursor(Object cursorHandle);
	
	public int getPbufferCapabilities() {
		lockAWT();
		int caps = nGetPbufferCapabilities();
		unlockAWT();
		return caps;
	}
	private static native int nGetPbufferCapabilities();

	public boolean isBufferLost(PeerInfo handle) {
		return false;
	}

/*	public void makePbufferCurrent(ByteBuffer handle) throws LWJGLException {
		lockAWT();
		try {
			nMakePbufferCurrent(handle);
		} finally {
			unlockAWT();
		}
	}
	
	private static native void nMakePbufferCurrent(ByteBuffer handle) throws LWJGLException;
*/
/*	public ByteBuffer createPbuffer(int width, int height, PixelFormat pixel_format,
			IntBuffer pixelFormatCaps,
			IntBuffer pBufferAttribs, ByteBuffer shared_pbuffer_handle) throws LWJGLException {
		lockAWT();
		try {
			ByteBuffer handle = BufferUtils.createByteBuffer(PBUFFER_HANDLE_SIZE);
			incDisplay();
			try {
				nCreatePbuffer(handle, width, height, pixel_format, pixelFormatCaps, pBufferAttribs, shared_pbuffer_handle);
				return handle;
			} catch (LWJGLException e) {
				decDisplay();
				throw e;
			}
		} finally {
			unlockAWT();
		}
	}

	private static native void nCreatePbuffer(ByteBuffer handle, int width, int height, PixelFormat pixel_format,
			IntBuffer pixelFormatCaps,
			IntBuffer pBufferAttribs, ByteBuffer shared_pbuffer_handle) throws LWJGLException;
*/
	public PeerInfo createPbuffer(int width, int height, PixelFormat pixel_format,
			IntBuffer pixelFormatCaps,
			IntBuffer pBufferAttribs) throws LWJGLException {
		lockAWT();
		try {
			incDisplay();
			try {
				GLContext.loadOpenGLLibrary();
				try {
					PeerInfo peer_info = new LinuxPbufferPeerInfo(width, height, pixel_format);
					return peer_info;
				} catch (LWJGLException e) {
					GLContext.unloadOpenGLLibrary();
					throw e;
				}
			} catch (LWJGLException e) {
				decDisplay();
				throw e;
			}
		} finally {
			unlockAWT();
		}
	}

	public void destroyPbuffer(PeerInfo handle) {
		lockAWT();
		((LinuxPbufferPeerInfo)handle).destroy();
		decDisplay();
		GLContext.unloadOpenGLLibrary();
		unlockAWT();
	}

	public void setPbufferAttrib(PeerInfo handle, int attrib, int value) {
		throw new UnsupportedOperationException();
	}

	public void bindTexImageToPbuffer(PeerInfo handle, int buffer) {
		throw new UnsupportedOperationException();
	}

	public void releaseTexImageFromPbuffer(PeerInfo handle, int buffer) {
		throw new UnsupportedOperationException();
	}
}
