package com.app.androidkt.googlevisionapi;


import android.util.Log;
import android.widget.Toast;

import com.google.api.services.vision.v1.model.AnnotateImageResponse;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesResponse;
import com.google.api.services.vision.v1.model.EntityAnnotation;
import com.google.api.services.vision.v1.model.Vertex;

import org.opencv.core.Rect;

import java.util.List;
import java.util.Vector;

// class that represents the data of a specific screen
public class ScreenData {

    private List<ScreenElement> elementList;
    private Rect screenBoundingBox;
    private String screenName;

    public String getName() { return screenName; }

    public ScreenData( BatchAnnotateImagesResponse response)
    {
        initialize(response);
    }

    public ScreenData(BatchAnnotateImagesResponse response, Rect boundBox)
    {
        screenBoundingBox=boundBox;
        initialize(response);
    }

    // for constructing search set screenData
    public ScreenData(List<String> text, List<List<Vertex>> vertices, String name)
    {
        elementList = new Vector<>();
        for(int i = 0; i < text.size(); i++)
        {
            ScreenElement element = new ScreenElement(text.get(i), vertices.get(i));
            elementList.add(element);
        }
        screenName = name;

    }

    private void initialize( BatchAnnotateImagesResponse response)
    {
        elementList = new Vector<>();

        AnnotateImageResponse imageResponses = response.getResponses().get(0);

        List<EntityAnnotation> entityAnnotations;
        entityAnnotations = imageResponses.getTextAnnotations();

        if (entityAnnotations != null) {
            for (EntityAnnotation entity : entityAnnotations) {
                String text = entity.getDescription();
                List<Vertex> vertices = entity.getBoundingPoly().getVertices();
                ScreenElement element;

                if(screenBoundingBox != null)
                    element = new ScreenElement(text, vertices, screenBoundingBox);
                else
                    element = new ScreenElement(text, vertices);

                elementList.add(element);

            }
        }
    }
    public boolean compareScreen(ScreenData inputScreen)
    {
        boolean sameScreen = false;
        int numElements = this.elementList.size();
        int correctElements = 0;
        //float vertexTreshold = 50;

        for(ScreenElement element : this.elementList)
        {

            for(ScreenElement inputElement : inputScreen.elementList)
            {
                boolean text_verified = false;
                if(!element.getText().equals("NULL")){
                    text_verified = textCompare(inputElement.getText(), element.getText());
                }
                else{
                    text_verified = true;
                }

                if(text_verified)
                {
                    correctElements++;
                    break;
                }
            }
        }
        Log.d("CompareScreenTag", Integer.toString(correctElements) );
        Log.d("CompareScreenTag", "++++++++++++++++++++++++++++++++++++" );
        if (correctElements == numElements)
        {
            sameScreen = true;
        }
        return  sameScreen;
    }

    private boolean textCompare(String inputText, String text)
    {
        boolean text_verified = false;
        // min number for viable levenshtein distance
        float textThreshold = (float) (((float) text.length())/2.0);

        int substring_start = 0;
        int substring_end = text.length();
        int distance = text.length();

        while(substring_end < inputText.length())
        {
            int new_distance = levenshteinDistance(inputText.substring(substring_start,substring_end), text);
            distance = Math.min(distance,new_distance);
            substring_end++;
            substring_start++;
        }


        if(distance < textThreshold)
        {
            Log.d("CompareScreenTag", "FOUND!!!!!!!!!!!!" );
            text_verified = true;
        }
        Log.d("CompareScreenTag", inputText + " , " + text);
        Log.d("CompareScreenTag", Integer.toString(distance) + " , " + Float.toString(textThreshold) );

        Log.d("CompareScreenTag", "______________" );
        return text_verified;
    }
    public void printScreenData(){
        for (ScreenElement element: elementList)
        {
            element.printScreenElement();
        }
    }
    // computes the minimum number of single-character edits required to change one word into the other. Strings do not have to be the same length
    public static int levenshteinDistance(String a, String b) {
        a = a.toLowerCase();
        b = b.toLowerCase();
        // i == 0
        int [] costs = new int [b.length() + 1];
        for (int j = 0; j < costs.length; j++)
            costs[j] = j;
        for (int i = 1; i <= a.length(); i++) {
            // j == 0; nw = lev(i - 1, j)
            costs[0] = i;
            int nw = i - 1;
            for (int j = 1; j <= b.length(); j++) {
                int cj = Math.min(1 + Math.min(costs[j], costs[j - 1]), a.charAt(i - 1) == b.charAt(j - 1) ? nw : nw + 1);
                nw = costs[j];
                costs[j] = cj;
            }
        }
        return costs[b.length()];
    }

}
