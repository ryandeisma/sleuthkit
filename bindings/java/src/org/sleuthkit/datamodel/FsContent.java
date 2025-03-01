/*
 * SleuthKit Java Bindings
 *
 * Copyright 2011-2022 Basis Technology Corp.
 * Contact: carrier <at> sleuthkit <dot> org
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
package org.sleuthkit.datamodel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.sleuthkit.datamodel.TskData.FileKnown;
import org.sleuthkit.datamodel.TskData.TSK_DB_FILES_TYPE_ENUM;
import org.sleuthkit.datamodel.TskData.TSK_FS_ATTR_TYPE_ENUM;
import org.sleuthkit.datamodel.TskData.TSK_FS_META_TYPE_ENUM;
import org.sleuthkit.datamodel.TskData.TSK_FS_NAME_FLAG_ENUM;
import org.sleuthkit.datamodel.TskData.TSK_FS_NAME_TYPE_ENUM;

/**
 * An abstract base class for representations of a file system files or
 * directories that have been added to a case.
 *
 * TODO move common getters to AbstractFile class
 */
public abstract class FsContent extends AbstractFile {

	private static final Logger logger = Logger.getLogger(FsContent.class.getName());
	private List<String> metaDataText = null;

	/**
	 *
	 * @deprecated Use getFileHandle instead.
	 */
	// TODO: Make private.
	@Deprecated
	protected volatile long fileHandle = 0;

	/**
	 * Constructs an abstract base class for representations of a file system
	 * files or directories that have been added to a case.
	 *
	 * @param db                 The case database to which the file has been
	 *                           added.
	 * @param objId              The object id of the file in the case database.
	 * @param dataSourceObjectId The object id of the data source for the file.
	 * @param fsObjId            The object id of the file system to which this
	 *                           file belongs.
	 * @param attrType           The type attribute given to the file by the
	 *                           file system.
	 * @param attrId             The type id given to the file by the file
	 *                           system.
	 * @param name               The name of the file.
	 * @param fileType           The type of file
	 * @param metaAddr           The meta address of the file.
	 * @param metaSeq            The meta sequence number of the file.
	 * @param dirType            The type of the file, usually as reported in
	 *                           the name structure of the file system. May be
	 *                           set to TSK_FS_NAME_TYPE_ENUM.UNDEF.
	 * @param metaType           The type of the file, usually as reported in
	 *                           the metadata structure of the file system. May
	 *                           be set to
	 *                           TSK_FS_META_TYPE_ENUM.TSK_FS_META_TYPE_UNDEF.
	 * @param dirFlag            The allocated status of the file, usually as
	 *                           reported in the name structure of the file
	 *                           system.
	 * @param metaFlags          The allocated status of the file, usually as
	 *                           reported in the metadata structure of the file
	 *                           system.
	 * @param size               The size of the file.
	 * @param ctime              The changed time of the file.
	 * @param crtime             The created time of the file.
	 * @param atime              The accessed time of the file.
	 * @param mtime              The modified time of the file.
	 * @param modes              The modes for the file.
	 * @param uid                The UID for the file.
	 * @param gid                The GID for the file.
	 * @param md5Hash            The MD5 hash of the file, null if not yet
	 *                           calculated.
	 * @param sha256Hash         sha256 hash of the file, or null if not present
	 * @param sha1Hash           SHA-1 hash of the file, or null if not present
	 * @param knownState         The known state of the file from a hash
	 *                           database lookup, null if not yet looked up.
	 * @param parentPath         The path of the parent of the file.
	 * @param mimeType           The MIME type of the file, null if it has not
	 *                           yet been determined.
	 * @param extension          The extension part of the file name (not
	 *                           including the '.'), can be null.
	 * @param ownerUid			 UID of the file owner as found in the file
	 *                           system, can be null.
	 * @param osAccountObjId	 Obj id of the owner OS account, may be null.
	 * @param collected          Collected status of the file data
	 */
	FsContent(SleuthkitCase db,
			long objId,
			long dataSourceObjectId,
			long fsObjId,
			TSK_FS_ATTR_TYPE_ENUM attrType, int attrId,
			String name,
			TSK_DB_FILES_TYPE_ENUM fileType,
			long metaAddr, int metaSeq,
			TSK_FS_NAME_TYPE_ENUM dirType, TSK_FS_META_TYPE_ENUM metaType,
			TSK_FS_NAME_FLAG_ENUM dirFlag, short metaFlags,
			long size,
			long ctime, long crtime, long atime, long mtime,
			short modes, int uid, int gid,
			String md5Hash, String sha256Hash, String sha1Hash,
			FileKnown knownState,
			String parentPath,
			String mimeType,
			String extension,
			String ownerUid,
			Long osAccountObjId,
			TskData.CollectedStatus collected,
			List<Attribute> fileAttributes) {
		super(db, objId, dataSourceObjectId, Long.valueOf(fsObjId), attrType, attrId, name, fileType, metaAddr, metaSeq, dirType, metaType, dirFlag, metaFlags, size, ctime, crtime, atime, mtime, modes, uid, gid, md5Hash, sha256Hash, sha1Hash, knownState, parentPath, mimeType, extension, ownerUid, osAccountObjId, collected, fileAttributes);
	}

	/**
	 * Get the object id of the parent file system of this file or directory.
	 *
	 * @return the parent file system id
	 */
	public long getFileSystemId() {
		return getFileSystemObjectId().orElse(0L);
	}

	/**
	 * Opens a JNI file handle for this file or directory.
	 *
	 * @throws TskCoreException if there is a problem opening the handle.
	 */
	@SuppressWarnings("deprecation")
	void loadFileHandle() throws TskCoreException {
		if (fileHandle == 0) {
			synchronized (this) {
				if (fileHandle == 0) {
					fileHandle = SleuthkitJNI.openFile(getFileSystem().getFileSystemHandle(), metaAddr, attrType, attrId, getSleuthkitCase());
				}
			}
		}
	}

	/**
	 * Gets the JNI file handle for this file or directory, zero if the file has
	 * not been opened by calling loadHandle.
	 *
	 * @return The JNI file handle.
	 */
	@SuppressWarnings("deprecation")
	long getFileHandle() {
		return fileHandle;
	}

	/**
	 * Reads bytes from this file or directory.
	 *
	 * @param buf    Buffer to read into.
	 * @param offset Start position in the file.
	 * @param len    Number of bytes to read.
	 *
	 * @return Number of bytes read.
	 *
	 * @throws TskCoreException if there is a problem reading the file.
	 */
	@Override
	@SuppressWarnings("deprecation")
	protected synchronized int readInt(byte[] buf, long offset, long len) throws TskCoreException {
		if (offset == 0 && size == 0) {
			//special case for 0-size file
			return 0;
		}
		loadFileHandle();
		return SleuthkitJNI.readFile(fileHandle, buf, offset, len);
	}

	@Override
	public boolean isRoot() {
		try {
			FileSystem fs = getFileSystem();
			return fs.getRoot_inum() == this.getMetaAddr();
		} catch (TskCoreException ex) {
			logger.log(Level.SEVERE, "Exception while calling 'getFileSystem' on " + this, ex); //NON-NLS
			return false;
		}
	}

	/**
	 * Gets the parent directory of this file or directory.
	 *
	 * @return The parent directory or null if there isn't one
	 *
	 * @throws TskCoreException if there was an error querying the case
	 *                          database.
	 */
	public AbstractFile getParentDirectory() throws TskCoreException {
		return getSleuthkitCase().getParentDirectory(this);
	}

	/**
	 * Gets the data source (image) for this file or directory directory.
	 *
	 * @return The data source.
	 *
	 * @throws TskCoreException if there is an error querying the case database.
	 */
	@Override
	public Content getDataSource() throws TskCoreException {
		return getFileSystem().getDataSource();
	}

	/**
	 * Gets a text-based description of the file's metadata. This is the same
	 * content as the TSK istat tool produces and is different information for
	 * each type of file system.
	 *
	 * @return List of text, one element per line.
	 *
	 * @throws TskCoreException
	 */
	public synchronized List<String> getMetaDataText() throws TskCoreException {
		if (metaDataText != null) {
			return metaDataText;
		}

		// if there is no metadata for this file, return empty string
		if (metaAddr == 0) {
			metaDataText = new ArrayList<String>();
			metaDataText.add("");
			return metaDataText;
		}

		loadFileHandle();
		metaDataText = SleuthkitJNI.getFileMetaDataText(fileHandle);
		return metaDataText;
	}

	/**
	 * Closes the JNI file handle for this file or directory.
	 */
	@Override
	@SuppressWarnings("deprecation")
	public synchronized void close() {
		if (fileHandle != 0) {
			SleuthkitJNI.closeFile(fileHandle);
			fileHandle = 0;
		}
	}

	/**
	 * Closes the JNI file handle for this file or directory when the FsContent
	 * object is garbage-collected.
	 */
	@Override
	public void finalize() throws Throwable {
		try {
			close();
		} finally {
			super.finalize();
		}
	}

	/**
	 * Provides a string representation of this file or directory.
	 *
	 * @param preserveState True if state should be included in the string
	 *                      representation of this object.
	 */
	@Override
	public String toString(boolean preserveState) {
		String path = "";
		try {
			path = getUniquePath();
		} catch (TskCoreException ex) {
			logger.log(Level.SEVERE, "Error loading unique path for object ID: {0}", this.getId());
		}
		
		return super.toString(preserveState)
				+ "FsContent [\t" //NON-NLS
				+ "fsObjId " + getFileSystemId() //NON-NLS
				+ "\t" + "uniquePath " + path //NON-NLS
				+ "\t" + "fileHandle " + getFileHandle() //NON-NLS
				+ "]\t";
	}

	/**
	 * Constructs an abstract base class for representations of a file system
	 * files or directories that have been added to a case.
	 *
	 * @param db         The case database to which the file has been added.
	 * @param objId      The object id of the file in the case database.
	 * @param fsObjId    The object id of the file system to which this file
	 *                   belongs.
	 * @param attrType   The type attribute given to the file by the file
	 *                   system.
	 * @param attrId     The type id given to the file by the file system.
	 * @param name       The name of the file.
	 * @param metaAddr   The meta address of the file.
	 * @param metaSeq    The meta sequence number of the file.
	 * @param dirType    The type of the file, usually as reported in the name
	 *                   structure of the file system. May be set to
	 *                   TSK_FS_NAME_TYPE_ENUM.UNDEF.
	 * @param metaType   The type of the file, usually as reported in the
	 *                   metadata structure of the file system. May be set to
	 *                   TSK_FS_META_TYPE_ENUM.TSK_FS_META_TYPE_UNDEF.
	 * @param dirFlag    The allocated status of the file, usually as reported
	 *                   in the name structure of the file system.
	 * @param metaFlags  The allocated status of the file, usually as reported
	 *                   in the metadata structure of the file system.
	 * @param size       The size of the file.
	 * @param ctime      The changed time of the file.
	 * @param crtime     The created time of the file.
	 * @param atime      The accessed time of the file.
	 * @param mtime      The modified time of the file.
	 * @param modes      The modes for the file.
	 * @param uid        The UID for the file.
	 * @param gid        The GID for the file.
	 * @param md5Hash    The MD5 hash of the file, null if not yet calculated.
	 * @param knownState The known state of the file from a hash database
	 *                   lookup, null if not yet looked up.
	 * @param parentPath The path of the parent of the file.
	 *
	 * @deprecated Do not make subclasses outside of this package.
	 */
	@Deprecated
	@SuppressWarnings("deprecation")
	FsContent(SleuthkitCase db, long objId, long fsObjId, TSK_FS_ATTR_TYPE_ENUM attrType, short attrId,
			String name, long metaAddr, int metaSeq, TSK_FS_NAME_TYPE_ENUM dirType, TSK_FS_META_TYPE_ENUM metaType,
			TSK_FS_NAME_FLAG_ENUM dirFlag, short metaFlags, long size, long ctime, long crtime, long atime, long mtime,
			short modes, int uid, int gid, String md5Hash, FileKnown knownState, String parentPath) {
		this(db, objId, db.getDataSourceObjectId(objId), fsObjId, attrType, (int) attrId, name, TSK_DB_FILES_TYPE_ENUM.FS, metaAddr, metaSeq, dirType, metaType, dirFlag, metaFlags, size, ctime, crtime, atime, mtime, modes, uid, gid, md5Hash, null, null, knownState, parentPath, null, null, OsAccount.NO_OWNER_ID, OsAccount.NO_ACCOUNT, Collections.emptyList());
	}

	/**
	 * Constructs an abstract base class for representations of a file system
	 * files or directories that have been added to a case. This deprecated
	 * version has attrId filed defined as a short which has since been changed
	 * to an int.
	 *
	 * @param db                 The case database to which the file has been
	 *                           added.
	 * @param objId              The object id of the file in the case database.
	 * @param dataSourceObjectId The object id of the data source for the file.
	 * @param fsObjId            The object id of the file system to which this
	 *                           file belongs.
	 * @param attrType           The type attribute given to the file by the
	 *                           file system.
	 * @param attrId             The type id given to the file by the file
	 *                           system.
	 * @param name               The name of the file.
	 * @param metaAddr           The meta address of the file.
	 * @param metaSeq            The meta sequence number of the file.
	 * @param dirType            The type of the file, usually as reported in
	 *                           the name structure of the file system. May be
	 *                           set to TSK_FS_NAME_TYPE_ENUM.UNDEF.
	 * @param metaType           The type of the file, usually as reported in
	 *                           the metadata structure of the file system. May
	 *                           be set to
	 *                           TSK_FS_META_TYPE_ENUM.TSK_FS_META_TYPE_UNDEF.
	 * @param dirFlag            The allocated status of the file, usually as
	 *                           reported in the name structure of the file
	 *                           system.
	 * @param metaFlags          The allocated status of the file, usually as
	 *                           reported in the metadata structure of the file
	 *                           system.
	 * @param size               The size of the file.
	 * @param ctime              The changed time of the file.
	 * @param crtime             The created time of the file.
	 * @param atime              The accessed time of the file.
	 * @param mtime              The modified time of the file.
	 * @param modes              The modes for the file.
	 * @param uid                The UID for the file.
	 * @param gid                The GID for the file.
	 * @param md5Hash            The MD5 hash of the file, null if not yet
	 *                           calculated.
	 * @param knownState         The known state of the file from a hash
	 *                           database lookup, null if not yet looked up.
	 * @param parentPath         The path of the parent of the file.
	 * @param mimeType           The MIME type of the file, null if it has not
	 *                           yet been determined.
	 *
	 * @deprecated Do not make subclasses outside of this package.
	 */
	@Deprecated
	@SuppressWarnings("deprecation")
	FsContent(SleuthkitCase db, long objId, long dataSourceObjectId, long fsObjId, TSK_FS_ATTR_TYPE_ENUM attrType, short attrId,
			String name, long metaAddr, int metaSeq, TSK_FS_NAME_TYPE_ENUM dirType, TSK_FS_META_TYPE_ENUM metaType,
			TSK_FS_NAME_FLAG_ENUM dirFlag, short metaFlags, long size, long ctime, long crtime, long atime, long mtime,
			short modes, int uid, int gid, String md5Hash, FileKnown knownState, String parentPath, String mimeType) {
		this(db, objId, dataSourceObjectId, fsObjId, attrType, (int) attrId, name, TSK_DB_FILES_TYPE_ENUM.FS, metaAddr, metaSeq, dirType, metaType, dirFlag, metaFlags, size, ctime, crtime, atime, mtime, modes, uid, gid, md5Hash, null, null, knownState, parentPath, mimeType, null, OsAccount.NO_OWNER_ID, OsAccount.NO_ACCOUNT, Collections.emptyList());
	}
	
		/**
	 * Constructs an abstract base class for representations of a file system
	 * files or directories that have been added to a case.
	 *
	 * @param db                 The case database to which the file has been
	 *                           added.
	 * @param objId              The object id of the file in the case database.
	 * @param dataSourceObjectId The object id of the data source for the file.
	 * @param fsObjId            The object id of the file system to which this
	 *                           file belongs.
	 * @param attrType           The type attribute given to the file by the
	 *                           file system.
	 * @param attrId             The type id given to the file by the file
	 *                           system.
	 * @param name               The name of the file.
	 * @param fileType           The type of file
	 * @param metaAddr           The meta address of the file.
	 * @param metaSeq            The meta sequence number of the file.
	 * @param dirType            The type of the file, usually as reported in
	 *                           the name structure of the file system. May be
	 *                           set to TSK_FS_NAME_TYPE_ENUM.UNDEF.
	 * @param metaType           The type of the file, usually as reported in
	 *                           the metadata structure of the file system. May
	 *                           be set to
	 *                           TSK_FS_META_TYPE_ENUM.TSK_FS_META_TYPE_UNDEF.
	 * @param dirFlag            The allocated status of the file, usually as
	 *                           reported in the name structure of the file
	 *                           system.
	 * @param metaFlags          The allocated status of the file, usually as
	 *                           reported in the metadata structure of the file
	 *                           system.
	 * @param size               The size of the file.
	 * @param ctime              The changed time of the file.
	 * @param crtime             The created time of the file.
	 * @param atime              The accessed time of the file.
	 * @param mtime              The modified time of the file.
	 * @param modes              The modes for the file.
	 * @param uid                The UID for the file.
	 * @param gid                The GID for the file.
	 * @param md5Hash            The MD5 hash of the file, null if not yet
	 *                           calculated.
	 * @param sha256Hash         sha256 hash of the file, or null if not present
	 * @param sha1Hash           SHA-1 hash of the file, or null if not present
	 * @param knownState         The known state of the file from a hash
	 *                           database lookup, null if not yet looked up.
	 * @param parentPath         The path of the parent of the file.
	 * @param mimeType           The MIME type of the file, null if it has not
	 *                           yet been determined.
	 * @param extension          The extension part of the file name (not
	 *                           including the '.'), can be null.
	 * @param ownerUid			 UID of the file owner as found in the file
	 *                           system, can be null.
	 * @param osAccountObjId	 Obj id of the owner OS account, may be null.

	 * @deprecated Do not make subclasses outside of this package.
	 */
	@Deprecated
	@SuppressWarnings("deprecation")
	FsContent(SleuthkitCase db,
			long objId,
			long dataSourceObjectId,
			long fsObjId,
			TSK_FS_ATTR_TYPE_ENUM attrType, int attrId,
			String name,
			TSK_DB_FILES_TYPE_ENUM fileType,
			long metaAddr, int metaSeq,
			TSK_FS_NAME_TYPE_ENUM dirType, TSK_FS_META_TYPE_ENUM metaType,
			TSK_FS_NAME_FLAG_ENUM dirFlag, short metaFlags,
			long size,
			long ctime, long crtime, long atime, long mtime,
			short modes, int uid, int gid,
			String md5Hash, String sha256Hash, String sha1Hash,
			FileKnown knownState,
			String parentPath,
			String mimeType,
			String extension,
			String ownerUid,
			Long osAccountObjId,
			List<Attribute> fileAttributes) {
		this(db, objId, dataSourceObjectId, fsObjId, attrType, attrId, name, TSK_DB_FILES_TYPE_ENUM.FS, metaAddr, metaSeq, dirType, metaType, dirFlag, metaFlags, size, ctime, crtime, atime, mtime, modes, uid, gid, md5Hash, null, null, knownState, parentPath, mimeType, null, OsAccount.NO_OWNER_ID, OsAccount.NO_ACCOUNT, TskData.CollectedStatus.UNKNOWN, Collections.emptyList());
	}
}
