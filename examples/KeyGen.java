/* -*-mode:java; c-basic-offset:2; indent-tabs-mode:nil -*- */
import com.jcraft.jsch.*;
import javax.swing.*;

class KeyGen{
  public static void main(String[] arg){
    if(arg.length<3){
      System.err.println(
"usage: java KeyGen rsa output_keyfile comment\n"+
"       java KeyGen dsa  output_keyfile comment");
      System.exit(-1);
    }
    String _type=arg[0];
    int type=0;
    if(_type.equals("rsa")){type=KeyPair.RSA;}
    else if(_type.equals("dsa")){type=KeyPair.DSA;}
    else {
      System.err.println(
"usage: java KeyGen rsa output_keyfile comment\n"+
"       java KeyGen dsa  output_keyfile comment");
      System.exit(-1);
    }
    String filename=arg[1];
    String comment=arg[2];

    JSch jsch=new JSch();

    String passphrase="";
    JTextField passphraseField=(JTextField)new JPasswordField(20);
    Object[] ob={passphraseField};
    int result=
      JOptionPane.showConfirmDialog(null, ob, "Enter passphrase (empty for no passphrase)",
				    JOptionPane.OK_CANCEL_OPTION);
    if(result==JOptionPane.OK_OPTION){
      passphrase=passphraseField.getText();
    }

    try{
      KeyPair kpair=KeyPair.genKeyPair(jsch, type);
      kpair.setPassphrase(passphrase);
      kpair.writePrivateKey(filename);
      kpair.writePublicKey(filename+".pub", comment);
      System.out.println("Finger print: "+kpair.getFingerPrint());
      kpair.dispose();
    }
    catch(Exception e){
      System.out.println(e);
    }
    System.exit(0);
  }
}
