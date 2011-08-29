package spl;

import java.util.List;

import org.sciplore.xml.XmlAuthor;
import org.sciplore.xml.XmlDocument;
import org.sciplore.xml.XmlKeyword;

/**
 * Created by IntelliJ IDEA.
 * User: Christoph Arbeit
 * Date: 10.09.2010
 * Time: 20:02:51
 * To change this template use File | Settings | File Templates.
 */
public class DocumentWrapper {

    XmlDocument xmlDocument;

    public DocumentWrapper(XmlDocument xmlDocument) {
        this.xmlDocument = xmlDocument;
    }

    public XmlDocument getXmlDocument() {
        return xmlDocument;
    }

    public void setXmlDocument(XmlDocument xmlDocument) {
        this.xmlDocument = xmlDocument;
    }

    public String getTitle(){
        if(this.hasTitle()){
            return xmlDocument.getTitle().getValue();
        }
        else{
            return "";
        }
    }
 // JABREF: isEmpty change
    public boolean hasTitle(){
        return (xmlDocument.getTitle() != null && xmlDocument.getTitle().getValue() != null && xmlDocument.getTitle().getValue().length() > 0);
    }

    public String getAbstract(){
        if(this.hasAbstract()){
            return xmlDocument.getAbstract().getValue();
        }
        else{
            return "";
        }
    }
 // JABREF: isEmpty change
    public boolean hasAbstract(){
        return (xmlDocument.getAbstract() != null && xmlDocument.getAbstract().getValue() != null && xmlDocument.getAbstract().getValue().length() > 0);
    }

    public String getAuthors(String seperator){
        if(this.hasAuthors()){
            List<XmlAuthor> authors = xmlDocument.getAuthors().getAuthors();
            String value = "";
            int i = 1;
            for(XmlAuthor author : authors){
                if(i < authors.size()){
                    value = value + author.getNameComplete();
                    value = value + seperator + " ";
                }
                else{
                    value = value + author.getNameComplete();
                }
                i++;
            }
            return value;
        }
        else{
            return "";
        }
    }

    public boolean hasAuthors(){
        return (xmlDocument.getAuthors() != null && xmlDocument.getAuthors().getAuthors() != null && !xmlDocument.getAuthors().getAuthors().isEmpty());
    }

    public String getKeyWords(){
        if(this.hasKeyWords()){
            List<XmlKeyword> keywords = xmlDocument.getKeywords().getKeywords();
            String value = "";
            int i = 1;
            for(XmlKeyword keyword : keywords){
                if(i < keywords.size()){
                    value = value + keyword.getValue();
                    value = value + ", ";
                }
                else{
                    value = value + keyword.getValue();
                }
                i++;
            }
            return value;
        }
        else{
            return "";
        }
    }

    public boolean hasKeyWords(){
        return (xmlDocument.getKeywords() != null && xmlDocument.getKeywords().getKeywords() != null && !xmlDocument.getKeywords().getKeywords().isEmpty());
    }


    public String getDoi(){
        if(this.hasDoi()){
            return xmlDocument.getDoi().getValue();
        }
        else{
            return "";
        }
    }
 // JABREF: isEmpty change
    public boolean hasDoi(){
        return (xmlDocument.getDoi() != null && xmlDocument.getDoi().getValue() != null && xmlDocument.getDoi().getValue().length() > 0);
    }

    public String getPages(){
        if(this.hasPages()){
            return xmlDocument.getPages().getValue();
        }
        else{
            return "";
        }
    }
 // JABREF: isEmpty change
    public boolean hasPages(){
        return (xmlDocument.getPages() != null && xmlDocument.getPages().getValue() != null && xmlDocument.getPages().getValue().length() > 0);
    }

    public String getVolume(){
        if(this.hasVolume()){
            return xmlDocument.getVolume().getValue();
        }
        else{
            return "";
        }
    }
 // JABREF: isEmpty change
    public boolean hasVolume(){
        return (xmlDocument.getVolume() != null && xmlDocument.getVolume().getValue() != null && xmlDocument.getVolume().getValue().length() > 0);
    }

    public String getNumber(){
        if(this.hasNumber()){
            return xmlDocument.getNumber().getValue();
        }
        else{
            return "";
        }
    }
 // JABREF: isEmpty change
    public boolean hasNumber(){
        return (xmlDocument.getNumber() != null && xmlDocument.getNumber().getValue() != null && xmlDocument.getNumber().getValue().length() > 0);
    }

    public String getYear(){
        if(this.hasYear()){
            return xmlDocument.getPublishdate().getYear();
        }
        else{
            return "";
        }
    }
 // JABREF: isEmpty change
    public boolean hasYear(){
        return (xmlDocument.getPublishdate() != null && xmlDocument.getPublishdate().getYear() != null && xmlDocument.getPublishdate().getYear().length() > 0 && !xmlDocument.getPublishdate().getYear().equalsIgnoreCase("null"));
    }

     public String getMonth(){
        if(this.hasMonth()){
            return xmlDocument.getPublishdate().getMonth();
        }
        else{
            return "";
        }
    }
  // JABREF: isEmpty change
    public boolean hasMonth(){
        return (xmlDocument.getPublishdate() != null && xmlDocument.getPublishdate().getMonth() != null && xmlDocument.getPublishdate().getMonth().length() > 0);
    }

    public String getDay(){
        if(this.hasDay()){
            return xmlDocument.getPublishdate().getDay();
        }
        else{
            return "";
        }
    }
 // JABREF: isEmpty change
    public boolean hasDay(){
        return (xmlDocument.getPublishdate() != null && xmlDocument.getPublishdate().getDay() != null && xmlDocument.getPublishdate().getDay().length() > 0);
    }

    public String getVenue() {
        if(this.hasVenue()){
            return xmlDocument.getVenue().getValue();
        }
        else{
            return "";
        }
    }
 // JABREF: isEmpty change
    public boolean hasVenue(){
        return (xmlDocument.getVenue() != null && xmlDocument.getVenue().getValue() != null && xmlDocument.getVenue().getValue().length() > 0);
    }
}
