package sptech.api_arquivos;

public enum Lambda {
    FILE_UPLOAD("poc-upload-img"),
    FILE_DOWNLOAD("poc-download-img");

    private final String name;

    Lambda(String name) {
        this.name = name;
    }

    public String getName(){
        return name;
    }
}
