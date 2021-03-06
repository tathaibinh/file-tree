package com.exadel.filetree.data;

import java.io.File;
import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: naXa!
 * Date: 14.06.13
 * Time: 17:27
 */

/**
 * Class is a structure, describing files and directories.
 *
 * @author naXa!
 */
public class FileIndex implements Comparable<FileIndex> {
    private String fullName;
    private Date lastModif;
    private Long size;
    private boolean file;

    /**
     * Default constructor
     */
    public FileIndex() {
        this.fullName = "";
        this.lastModif = new Date();
        this.size = 0L;
        this.file = true;
    }

    /**
     * Big constructor.
     * 
     * @param fullFileName Name of a file
     * @param lastModif Date of file's latest modification
     * @param size Size of a file in bytes
     * @param file Is it a file?
     */
    public FileIndex(String fullFileName, Date lastModif, Long size, boolean file) {
        this.fullName = fullFileName;
        this.lastModif = lastModif;
        this.size = size;
        this.file = file;
    }

    /**
     * Big constructor - 2.
     * 
     * @param fullFileName
     * @param lastModif long -> Date
     * @param size
     * @param file
     */
    public FileIndex(String fullFileName, long lastModif, Long size, boolean file) {
        this.fullName = fullFileName;
        this.lastModif = new Date( lastModif );
        this.size = size;
        this.file = file;
    }

    /**
     * Constructor for directories.
     *
     * @param fullFileName Name of a directory
     * @param lastModif Date of latest modification (long)
     */
    public FileIndex(String fullFileName, long lastModif) {
        this.fullName = fullFileName;
        this.lastModif = new Date( lastModif );
        this.size = 0L;     // ? not sure
        this.file = false;
    }

    /**
     * Easy constructor. Converts File to FileIndex.
     *
     * @param file File
     */
    public FileIndex(File file) {
        this.fullName = file.getAbsolutePath();
        this.lastModif = new Date( file.lastModified() );
        this.size = file.length();
        this.file = file.isFile();
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getFileName() {
        String fullName = getFullName();
        Integer separatorPos = fullName.lastIndexOf( File.separator );
        return (separatorPos == -1)? fullName : fullName.substring( separatorPos + 1 );
    }

    public String getPath() {
        String fullName = getFullName();
            // -2 is needed to skip a possible separator ('\') at the end of URI:
        Integer separatorPos = fullName.lastIndexOf( File.separator, fullName.length() - 2 );
        return (separatorPos == -1)? fullName : fullName.substring( 0, separatorPos );
    }

    public Date getLastModif() {
        return lastModif;
    }

    public void setLastModif(Date lastModif) {
        this.lastModif = lastModif;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public boolean isFile() {
        return file;
    }

    public void setFile(boolean file) {
        this.file = file;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof FileIndex))
            return false;

        FileIndex fileIndex = (FileIndex)obj;
        return !(!getFullName().equals( fileIndex.getFullName() ) ||
                 isFile() != fileIndex.isFile());
    }

    @Override
    public int hashCode() {
        return fullName.hashCode();
    }

    @Override
    public String toString() {
        return getFullName();
    }

    @Override
    public int compareTo(FileIndex o) {
        return this.toString().compareTo( o.toString() );
    }
}
