package simpledb;

import java.io.*;
import java.util.*;

/**
 * HeapFile is an implementation of a DbFile that stores a collection of tuples
 * in no particular order. Tuples are stored on pages, each of which is a fixed
 * size, and the file is simply a collection of those pages. HeapFile works
 * closely with HeapPage. The format of HeapPages is described in the HeapPage
 * constructor.
 * 
 * @see simpledb.HeapPage#HeapPage
 * @author Sam Madden
 */
public class HeapFile implements DbFile {

	private File file;

	private TupleDesc td;

	/**
	 * Constructs a heap file backed by the specified file.
	 * 
	 * @param f
	 *            the file that stores the on-disk backing store for this heap
	 *            file.
	 */
	public HeapFile(File f, TupleDesc td) {
		// some code goes here
		this.file = f;
		this.td = td;
	}

	/**
	 * Returns the File backing this HeapFile on disk.
	 * 
	 * @return the File backing this HeapFile on disk.
	 */
	public File getFile() {
		// some code goes here
		return this.file;
	}

	/**
	 * Returns an ID uniquely identifying this HeapFile. Implementation note:
	 * you will need to generate this tableid somewhere ensure that each
	 * HeapFile has a "unique id," and that you always return the same value for
	 * a particular HeapFile. We suggest hashing the absolute file name of the
	 * file underlying the heapfile, i.e. f.getAbsoluteFile().hashCode().
	 * 
	 * @return an ID uniquely identifying this HeapFile.
	 */
	public int getId() {
		// some code goes here
		return this.file.getAbsoluteFile().hashCode();
	}

	/**
	 * Returns the TupleDesc of the table stored in this DbFile.
	 * 
	 * @return TupleDesc of this DbFile.
	 */
	public TupleDesc getTupleDesc() {
		// some code goes here
		return this.td;
	}

	// see DbFile.java for javadocs
	public Page readPage(PageId pid) {
		// some code goes here
		try {
			byte[] data = new byte[BufferPool.getPageSize()];
			RandomAccessFile f = new RandomAccessFile(this.file, "r");
			HeapPageId hpId = new HeapPageId(pid.getTableId(), pid.pageNumber());
			long pos = BufferPool.getPageSize() * pid.pageNumber();
			f.seek(pos);
			f.readFully(data);
			f.close();
			return new HeapPage(hpId, data);
		} catch (FileNotFoundException e) {
			System.out.println("File " + this.file + " not found at readPage in HeapFile");
			// e.printStackTrace();
			throw new IllegalArgumentException();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.print("IOException: " + e.getMessage() + " at readPage in HeapFile");
			// e.printStackTrace();
			throw new IllegalArgumentException();
		}
	}

	// see DbFile.java for javadocs
	public void writePage(Page page) throws IOException {
		// some code goes here
		// not necessary for lab1
	}

	/**
	 * Returns the number of pages in this HeapFile.
	 */
	public int numPages() {
		// some code goes here
		return (int) Math.ceil(this.file.length() / (double) BufferPool.getPageSize());
	}

	// see DbFile.java for javadocs
	public ArrayList<Page> insertTuple(TransactionId tid, Tuple t)
			throws DbException, IOException, TransactionAbortedException {
		// some code goes here
		return null;
		// not necessary for lab1
	}

	// see DbFile.java for javadocs
	public Page deleteTuple(TransactionId tid, Tuple t) throws DbException, TransactionAbortedException {
		// some code goes here
		return null;
		// not necessary for lab1
	}

	// see DbFile.java for javadocs
	public DbFileIterator iterator(TransactionId tid) {
		return new HeapFileIterator(this, tid);
	}

	private class HeapFileIterator implements DbFileIterator {

		private HeapFile heapFile;

		private int pageIndex;

		private Iterator<Tuple> curIterator;

		private TransactionId tid;

		public HeapFileIterator(HeapFile heapFile, TransactionId tid) {
			this.heapFile = heapFile;
			this.tid = tid;
		}

		@Override
		public void open() throws DbException, TransactionAbortedException {
			// TODO Auto-generated method stub
			this.pageIndex = 0;
			HeapPageId hpId = new HeapPageId(heapFile.getId(), pageIndex);
			HeapPage hp = (HeapPage) Database.getBufferPool().getPage(this.tid, hpId, Permissions.READ_ONLY);
			this.curIterator = hp.iterator();
		}

		@Override
		public boolean hasNext() throws DbException, TransactionAbortedException {
			// TODO Auto-generated method stub
			if (this.curIterator == null)
				return false;
			if (this.curIterator.hasNext())
				return true;

			this.pageIndex++;
			// remeber to check this
			// otherwise, it will cause one more 'readPage' so that 
			// the ScanTest will fail
			if ( this.pageIndex >= this.heapFile.numPages() )
				return false;

			HeapPageId hpId = new HeapPageId(heapFile.getId(), pageIndex);
			HeapPage hp = (HeapPage) Database.getBufferPool().getPage(this.tid, hpId, Permissions.READ_ONLY);
			if (hp == null) {
				this.curIterator = null;
				return false;
			}

			this.curIterator = hp.iterator();
			if (this.curIterator.hasNext()) {
				return true;
			}

			this.curIterator = null;
			return false;
		}

		@Override
		public Tuple next() throws DbException, TransactionAbortedException, NoSuchElementException {
			// TODO Auto-generated method stub
			if (this.curIterator == null)
				throw new NoSuchElementException();
			if (hasNext()) {
				return this.curIterator.next();
			}
			throw new NoSuchElementException();
		}

		@Override
		public void rewind() throws DbException, TransactionAbortedException {
			// TODO Auto-generated method stub
			close();
			open();
		}

		@Override
		public void close() {
			// TODO Auto-generated method stub
			this.curIterator = null;
		}
	}

}
