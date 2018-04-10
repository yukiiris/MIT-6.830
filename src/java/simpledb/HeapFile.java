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
        file = f;
        this.td = td;
    }

    /**
     * Returns the File backing this HeapFile on disk.
     * 
     * @return the File backing this HeapFile on disk.
     */
    public File getFile() {
        // some code goes here
        return file;
    }

    /**
     * Returns an ID uniquely identifying this HeapFile. Implementation note:
     * you will need to generate this tableid somewhere to ensure that each
     * HeapFile has a "unique id," and that you always return the same value for
     * a particular HeapFile. We suggest hashing the absolute file name of the
     * file underlying the heapfile, i.e. f.getAbsoluteFile().hashCode().
     * 
     * @return an ID uniquely identifying this HeapFile.
     */
    public int getId() {
        // some code goes here
        return file.getAbsoluteFile().hashCode();
    }

    /**
     * Returns the TupleDesc of the table stored in this DbFile.
     * 
     * @return TupleDesc of this DbFile.
     */
    public TupleDesc getTupleDesc() {
        // some code goes here
        return td;
    }

    // see DbFile.java for javadocs
    public Page readPage(PageId pid) {
        // some code goes here
        if (pid.getTableId() != this.getId() || pid.getPageNumber() > numPages() || pid.getPageNumber() < 0) {
            return null;
        }
        try {
            int pageSize = BufferPool.getPageSize();
            byte[] bytes = new byte[pageSize];
            RandomAccessFile randomAccessFile = new RandomAccessFile(file, "r");
            randomAccessFile.seek(pageSize * pid.getPageNumber());
            randomAccessFile.readFully(bytes);
            randomAccessFile.close();
            return new HeapPage(new HeapPageId(pid.getTableId(), pid.getPageNumber()), bytes);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
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
        return (int)Math.ceil((double)file.length() / BufferPool.getPageSize());
    }

    // see DbFile.java for javadocs
    public ArrayList<Page> insertTuple(TransactionId tid, Tuple t)
            throws DbException, IOException, TransactionAbortedException {
        // some code goes here
        return null;
        // not necessary for lab1
    }

    // see DbFile.java for javadocs
    public ArrayList<Page> deleteTuple(TransactionId tid, Tuple t) throws DbException,
            TransactionAbortedException {
        // some code goes here
        return null;
        // not necessary for lab1
    }

    // see DbFile.java for javadocs
    public DbFileIterator iterator(TransactionId tid) {
        // some code goes here
        return new HeapFileIterator(this, tid, getId());
    }


    public class HeapFileIterator implements DbFileIterator {
        private TransactionId tid;
        private HeapFile hf;
        private int tableId;
        private boolean active;
        private int currentPageNo;
        private Iterator<Tuple> currentPageIter;

        public HeapFileIterator(HeapFile hf, TransactionId tid, int tableId) {
            this.tid = tid;
            this.hf = hf;
            this.tableId = tableId;
            close();
        }

        private int numPages() {
            return hf.numPages();
        }

        public void open() throws DbException, TransactionAbortedException {
            active = true;
            currentPageNo = -1;
            currentPageIter = null;
            while (currentPageNo + 1 < numPages()) {
                currentPageNo ++;
                currentPageIter = ((HeapPage)Database.getBufferPool().getPage(
                        tid, new HeapPageId(tableId, currentPageNo), Permissions.READ_ONLY)).iterator();
                if (!hasNext()) continue;
                return;
            }
        }

        public boolean hasNext() throws DbException, TransactionAbortedException {
            return (currentPageIter != null) && (currentPageIter.hasNext());
        }

        public Tuple next() throws DbException, TransactionAbortedException, NoSuchElementException {
            if (!active) throw new NoSuchElementException("Iterator has not been opened.");
            Tuple ans = (hasNext()) ? currentPageIter.next() : null;
            if (!hasNext()) {
                while (currentPageNo + 1 < numPages()) {
                    currentPageNo ++;
                    currentPageIter = ((HeapPage)Database.getBufferPool().getPage(
                            tid, new HeapPageId(tableId, currentPageNo), Permissions.READ_ONLY)).iterator();
                    if (!hasNext()) continue;
                    break;
                }
            }
            return ans;
        }

        public void rewind() throws DbException, TransactionAbortedException {
            if (!active) throw new DbException("Iterator has not been opened.");
            close();
            open();
        }

        public TupleDesc getTupleDesc() {
            return hf.getTupleDesc();
        }

        public void close() {
            active = false;
            currentPageNo = -1;
            currentPageIter = null;
        }
    }

}

