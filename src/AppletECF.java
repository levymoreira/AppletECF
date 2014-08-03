/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;
import jACBrFramework.ACBrException;
import jACBrFramework.serial.ecf.ACBrECF;
import jACBrFramework.serial.ecf.FormaPagamento;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 *
 * @author jose.lml
 */
public class AppletECF extends javax.swing.JApplet {
    
    ACBrECF ecf = null;
    BasicDBObject cupomFiscal = new BasicDBObject(); 
    ArrayList<BasicDBObject> itens = new ArrayList<>();
    
    /**
     * Initializes the applet AppletECF
     */
    @Override
    public void init() {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(AppletECF.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(AppletECF.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(AppletECF.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(AppletECF.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the applet */
        try {
            java.awt.EventQueue.invokeAndWait(new Runnable() {
                public void run() {
                    initComponents();
                    callBackJS();
                }
            });
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    public void callBackJS(){
        try {
            getAppletContext().showDocument(new URL("javascript:appletLoaded()"));
        } catch (MalformedURLException e) {
            System.err.println("Failed to call JavaScript function appletLoaded()");
        }
    }
    
    public String ativar(){
        jTextArea1.setText(jTextArea1.getText() + "\nativar");
       
        String retorno = "ECF Ativado com sucesso!";
        try {
            if(ecf!=null && ecf.getAtivo()){
                return "ECF ja se encontra ativado!";
            }
            // jTextArea1.setText(jTextArea1.getText() + "\npolicy :"+System.getProperty("java.security.policy"));   
            ecf = new ACBrECF();  
            ecf.setModelo(4); //daruma
            ecf.getDevice().setPorta("COM8");
            ecf.getDevice().setBaud(115200);            
            ecf.ativar();
            ecf.carregaFormasPagamento();
            
            for (int i = 0; i < ecf.getFormasPagamento().length; i++){
                FormaPagamento formaPagto = ecf.getFormasPagamento()[i];
                System.out.println(" - " + formaPagto.getIndice() + " " + formaPagto.getDescricao());
            }
        } catch (Exception e) {
            retorno = e.getMessage();           
        }
        return retorno;
    }
    
    public String vendeItem() {
        jTextArea1.setText(jTextArea1.getText() + "\nvendeItem");
        String retorno = "";
        try {
            if(itens.size()==0){ //primeiro item abre a venda               
               ecf.abreCupom();
               cupomFiscal.put("ccf", 1);
               cupomFiscal.put("coo", 1);
               cupomFiscal.put("data_inicio", new SimpleDateFormat("dd/MM/yyyy hh:mm:ss").format(new Date()));
            }

           ecf.vendeItem("0001", "PRODUTO UM", "I", 3, 0.8, 10, "UN", "%", "D", 0);
           BasicDBObject item = new BasicDBObject();        
           item.put("codigo", 123);
           item.put("quantidade", 1);
           item.put("valor_unitario", 15);
           itens.add(item);

        } catch (Exception e) {
            retorno = "Erro (vendeItem): " + e.getMessage();
        }
        return retorno;
    }
    
    public String subtotalizaCupom(){        
        jTextArea1.setText(jTextArea1.getText() + "\nsubtotalizaCupom");
        
        String retorno = "";
        try {
            ecf.subtotalizaCupom(0, "Mensagem subtotaliza");       
            cupomFiscal.put("subtotalizador", 15);
        } catch (Exception e) {
            retorno = "Erro (subtotalizaCupom): " + e.getMessage();
        }
        return retorno;
    }
    
    public String efetuaPagamento() {
        jTextArea1.setText(jTextArea1.getText() + "\nefetuaPagamento");
        
        String retorno = "";
        try {
            FormaPagamento formaPagto = ecf.getFormasPagamento()[0];
            ecf.efetuaPagamento(formaPagto.getIndice(), 50, "MENSAGEM FORMA DE PAGTO", false);
            cupomFiscal.put("index", 1);
            cupomFiscal.put("descricao", "A VISTA");
            cupomFiscal.put("valor", 15);
        } catch (Exception e) {
            retorno = "Erro (efetuaPagamento): " + e.getMessage();
        }
        return retorno;
    }
    
    public String fechaCupom() throws ACBrException {
        jTextArea1.setText(jTextArea1.getText() + "\nfechaCupom b");
        
        String retorno = "";
        try {
            ecf.fechaCupom("MENSAGEM FECHAMENTO");
        } catch (Exception e) {
            retorno = "Erro (fechaCupom): " + e.getMessage();
        }
        return retorno;// ;;salvarCupomFiscal();        
    }
    
    public String getJSON(){
        BasicDBObject cf = cupomFiscal;
        cf.put("itens", itens);
        return new BasicDBObject().toString();
    }
    
    public String verificarStatus(){    
        jTextArea1.setText(jTextArea1.getText() + "\nverificarStatus");
        
        Boolean ativo = false;
        try {
            ativo = ecf.getAtivo();
        } catch (Exception e) {
            return "Erro (verificarStatus):" + e.getMessage();
        }
        
        return ativo?"ECF ativo!":"ECF nao esta ativo!";        
    }
    
    public String cancelaCupom(){
        jTextArea1.setText(jTextArea1.getText() + "\ncancelaCupom");
        
        String retorno = "";
        try {
            ecf.cancelaCupom();
        } catch (Exception e) {
            retorno = "Erro (cancelaCupom): " + e.getMessage();
        }
        return retorno;
    }
    /*
        -- create file mongo.config on c:\data

        ##store data here
        dbpath=C:\data\db

        ##all output go here
        logpath=C:\data\mongo.log

        ##log read and write operations
        diaglog=3


        --create c:/data/db folder
        mongod --config C:\data\mongo.config --install
        net start MongoDB
    */
    
    public String iniciarMongoDB(){        
        try {
            String so = "windows"; //definir qual so o applet esta rodando
            if(so.equals("windows")){             
                (new File("C:\\data\\db")).mkdirs(); //cria a pasta pois o mongodb n cria pra vc

                FileWriter arquivo = new FileWriter(new File("C:\\data\\mongo.config")); //cria o arquivo de configuracao
                arquivo.write("dbpath=C:\\data\\db");
                arquivo.write("logpath=C:\\data\\mongo.log");
                arquivo.write("diaglog=3");
                arquivo.close();

                Runtime.getRuntime().exec("cmd.exe /c mongod --config C:\\data\\mongo.config --install"); //instala como servico no windows
                Thread.sleep(400);
                Runtime.getRuntime().exec("cmd.exe /c net start MongoDB"); //starta o servico
                Thread.sleep(400);
            }
        } catch (Exception e) {
            return "Erro: " + e.getMessage();
        }
        return "";
    }
        
    public String salvarCupomFiscal(){
        MongoClient mongo = null;
        try {
            mongo = new MongoClient( "192.168.252.10" , 27017 );   
        }catch (Exception ex) {            
        }
        
        try {           
            if(mongo==null){ // se nao achou o mongo db no servidor salva local
                mongo = new MongoClient( "localhost" , 27017 );
            }
        } catch (Exception ex) {
            return "Nao foi possivel conectar a base local! Verifique se o MongoDB esta instalado no seu computador. Erro: " + ex.getMessage();
        }

        DB db = mongo.getDB("cupons_db");
        DBCollection cupons = db.getCollection("cupons");        
        
        cupomFiscal.put("itens", new ArrayList<BasicDBObject>());        
        cupons.insert(cupomFiscal);        
        return "";
    }
    
    /**
     * This method is called from within the init() method to initialize the
     * form. WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();

        jTextArea1.setColumns(20);
        jTextArea1.setFont(new java.awt.Font("Lucida Console", 0, 10)); // NOI18N
        jTextArea1.setRows(5);
        jTextArea1.setText("Comandos: ");
        jScrollPane1.setViewportView(jTextArea1);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 392, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 490, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextArea jTextArea1;
    // End of variables declaration//GEN-END:variables
}
