package skpq;

@SuppressWarnings("rawtypes")
public class ScoredObject implements Comparable{
    private int id;
    private double score;
    private double distance;
    private double latitude;
    private double longitude;
    private String uri;        
   
    public ScoredObject(int id, double lat, double lgt){
        this(id, lat, lgt, null);
    }
    
    public ScoredObject(int id, double lat, double lgt, String uri){
        this.id = id;
        this.latitude = lat;
        this.longitude = lgt;
        this.uri = uri;
    }
          
    public int getId() {
        return id;
    }
    
    public double getLatitude() {
        return latitude;
    }
    
    public double getLongitude() {
        return longitude;
    }

    public void setMessage(String msg){
        this.uri =msg;
    }
    
    public String getMessage() {
        return uri;
    }
    
    public void setScore(double score) {
        this.score = score;
        /*
         double kd = k*((1-b)+(b*(wd/wa)));
         double wqt = Math.log((n-ft+0.5)/(ft+0.5));
         double wdt = ((k+1)*fdt)/(kd+fdt);
         this.score = wdt*wqt;
         /*
         n = numero de documentos na colecao
         ft = numero de documentos que contem o termo
         fdt = frequencia do termo no documento
         wd = tamanho do documento
         wa = tamanho medio do documento
         */
    }
    
    public double getScore() {
        return this.score;
    }

    public void setDistancia(double distance) {
        this.distance = distance;
    }
   
    public double getDistancia() {
        return this.distance;
    }
    
    public int compareTo(Object other) {
        if(other instanceof ScoredObject){
            ScoredObject otherDocument = (ScoredObject) other;
            double thisScore = this.getScore();
            double otherScore = otherDocument.getScore();
            if(thisScore > otherScore){
                return 1;
            }else if(thisScore < otherScore){
                return -1;
            }else{//They are equals
                ScoredObject outro = (ScoredObject) other;
                return this.getId() -outro.getId();
                
            }
        }
        throw new UnsupportedOperationException("Not supported yet.");
    }
       
    public String toString(){
        return "[id="+id+", score="+score+", lat="+latitude+", lgt="+longitude+", dist="+distance+", msg="+uri+"]";
    }
  
}

