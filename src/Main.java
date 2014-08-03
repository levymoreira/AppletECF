/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */



import jACBrFramework.ACBrException;
import jACBrFramework.serial.ecf.ACBrECF;
import jACBrFramework.serial.ecf.FormaPagamento;

/**
 *
 * @author levy
 */
public class Main {
    public static void main(String [] args) throws ACBrException{
        System.out.println("Inicianto");
        ACBrECF  ecf = null;
        ecf = new ACBrECF();  
            ecf.setModelo(4); //daruma
            ecf.getDevice().setPorta("COM8");
            ecf.getDevice().setBaud(115200);            
            ecf.ativar();
            ecf.carregaFormasPagamento();
            
        ecf.abreCupom();
        ecf.vendeItem("0001", "PRODUTO UM", "I", 3, 0.8, 10, "UN", "%", "D", 0);
        ecf.subtotalizaCupom(0, "Mensagem subtotaliza");
        FormaPagamento formaPagto = ecf.getFormasPagamento()[0];
	ecf.efetuaPagamento(formaPagto.getIndice(), 50, "MENSAGEM FORMA DE PAGTO", false);
        ecf.fechaCupom("MENSAGEM FECHAMENTO");
      
    }
}
