package com.silentcoders.abdulqadir.classes;

/**
 * Created by Sohail on 31-01-2018.
 */

public class BillHTML {

    String htmlHead, htmlTitle, htmlBody;

    public String wrapHTMLTag( String data ){
        return String.format( "<html>%s</html>", data );
    }

    public void setHTMLHead(){
        htmlHead = String.format( "<head>%s</head>", getHTMLTitle() );
    }

    public void setHTMLTitle( String title ){
        htmlTitle = String.format( "<title>%s</title>", title );
    }

    public String getHTMLTitle(){
        return htmlTitle;
    }

    public void setHTMLBody( String body ){
        htmlBody = String.format( "<body>%s</body>", body );
    }



}
