/*
 * Copyright 2013 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.jimfs.internal;

import static com.google.common.base.Preconditions.checkPositionIndexes;

import java.io.IOException;
import java.io.OutputStream;

/**
 * {@link OutputStream} for writing to a file's {@link ByteStore}.
 *
 * @author Colin Decker
 */
final class JimfsOutputStream extends OutputStream {

  private final Object lock = new Object();

  private File file;
  private ByteStore store;
  private final boolean append;

  private int pos;

  JimfsOutputStream(File file, boolean append) {
    this.file = file;
    this.store = file.content();
    this.append = append;
  }

  @Override
  public void write(int b) throws IOException {
    synchronized (lock) {
      checkNotClosed();

      if (append) {
        store.append((byte) b);
      } else {
        store.write(pos++, (byte) b);
      }

      file.updateModifiedTime();
    }
  }

  @Override
  public void write(byte[] b) throws IOException {
    synchronized (lock) {
      checkNotClosed();

      if (append) {
        store.append(b);
      } else {
        pos += store.write(pos, b);
      }

      file.updateModifiedTime();
    }
  }

  @Override
  public synchronized void write(byte[] b, int off, int len) throws IOException {
    checkPositionIndexes(off, off + len, b.length);
    synchronized (lock) {
      checkNotClosed();

      if (append) {
        store.append(b, off, len);
      } else {
        pos += store.write(pos, b, off, len);
      }

      file.updateModifiedTime();
    }
  }

  private void checkNotClosed() throws IOException {
    if (store == null) {
      throw new IOException("stream is closed");
    }
  }

  @Override
  public void close() throws IOException {
    synchronized (lock) {
      file = null;
      store = null;
    }
  }
}