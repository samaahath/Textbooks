package com.bookviewer.myapplication;

public class booklistitems {
    public String BookId;
    public String BookName;
    public String CoverimgUrl;
    public String BookUrl;
    public String Type;
    public String Pagenumber;
    public String GradeId;
    public String Entrydate;

    public booklistitems(String BookId, String BookName,String CoverimgUrl, String BookUrl,String Type,String Pagenumber, String GradeId, String Entrydate ) {
        this.BookId = BookId;
        this.BookName = BookName;
        this.CoverimgUrl = CoverimgUrl;
        this.BookUrl = BookUrl;
        this.Type = Type;
        this.Pagenumber = Pagenumber;
        this.GradeId = GradeId;
        this.Entrydate = Entrydate;
    }

    public String getBookId() {
        return BookId;
    }
    public String getBookName() {
        return BookName;
    }
    public String getCoverimgUrl() {
        return CoverimgUrl;
    }

    public String getBookUrl() {
        return BookUrl;
    }
    public String getType() {
        return Type;
    }
    public String getPagenumber() {
        return Pagenumber;
    }
    public String getGradeId() {
        return GradeId;
    }
    public String getEntrydate() {
        return Entrydate;
    }
}
