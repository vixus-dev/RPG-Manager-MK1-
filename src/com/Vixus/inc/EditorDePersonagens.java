package com.Vixus.inc;

import javax.swing.*;

import com.Vixus.inc.RPGManager.*;

import java.awt.*;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class EditorDePersonagens extends JDialog {

    private final RPGManager owner;
    private final List<Jogador> listaJogadores;
    private final String caminhoArquivo = "src/com/Vixus/inc/resources/jogadores.txt";

    private JComboBox<String> seletorJogador;
    private JTextField txtHp, txtAtk, txtEnergia, txtVelocidade, txtFormaPontos;
    private JTextField txtPercentual;
    private JCheckBox chkHp, chkAtk, chkEnergia, chkVelocidade;
    private JButton btnAplicarPercentual;

    public EditorDePersonagens(RPGManager owner, List<Jogador> listaJogadores) {
        super(owner, "Editor de Personagens", true);
        this.owner = owner;
        this.listaJogadores = listaJogadores;

        setSize(500, 450);
        setLocationRelativeTo(owner);
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // --- PAINEL DE SELEÇÃO E ATRIBUTOS ---
        gbc.gridx = 0; gbc.gridy = 0; add(new JLabel("Personagem:"), gbc);
        seletorJogador = new JComboBox<>();
        seletorJogador.addItem("Todos os Jogadores");
        for (Jogador j : listaJogadores) {
            seletorJogador.addItem(j.getNome());
        }
        gbc.gridx = 1; gbc.gridwidth = 2; add(seletorJogador, gbc);
        gbc.gridwidth = 1;

        gbc.gridx = 0; gbc.gridy = 1; add(new JLabel("HP:"), gbc);
        txtHp = new JTextField(15);
        gbc.gridx = 1; gbc.gridwidth = 2; add(txtHp, gbc);

        gbc.gridx = 0; gbc.gridy = 2; add(new JLabel("ATK:"), gbc);
        txtAtk = new JTextField(15);
        gbc.gridx = 1; gbc.gridwidth = 2; add(txtAtk, gbc);
        
        gbc.gridx = 0; gbc.gridy = 3; add(new JLabel("Energia:"), gbc);
        txtEnergia = new JTextField(15);
        gbc.gridx = 1; gbc.gridwidth = 2; add(txtEnergia, gbc);

        gbc.gridx = 0; gbc.gridy = 4; add(new JLabel("Velocidade:"), gbc);
        txtVelocidade = new JTextField(15);
        gbc.gridx = 1; gbc.gridwidth = 2; add(txtVelocidade, gbc);

        gbc.gridx = 0; gbc.gridy = 5; add(new JLabel("P. de Forma:"), gbc);
        txtFormaPontos = new JTextField(15);
        gbc.gridx = 1; gbc.gridwidth = 2; add(txtFormaPontos, gbc);

        // --- PAINEL DE AUMENTO PERCENTUAL ---
        gbc.gridy = 6; gbc.gridwidth = 3; add(new JSeparator(), gbc);

        gbc.gridy = 7; gbc.gridwidth = 1; add(new JLabel("Aumentar em (%):"), gbc);
        txtPercentual = new JTextField(5);
        gbc.gridx = 1; add(txtPercentual, gbc);
        
        btnAplicarPercentual = new JButton("Aplicar Aumento");
        gbc.gridx = 2; add(btnAplicarPercentual, gbc);

        chkHp = new JCheckBox("HP");
        chkAtk = new JCheckBox("ATK");
        chkEnergia = new JCheckBox("Energia");
        chkVelocidade = new JCheckBox("Velocidade");
        JPanel painelChecks = new JPanel(new FlowLayout(FlowLayout.CENTER));
        
        painelChecks.add(chkHp);
        painelChecks.add(chkAtk);
        painelChecks.add(chkEnergia);
        painelChecks.add(chkVelocidade);
        gbc.gridy = 8; gbc.gridx = 0; gbc.gridwidth = 3; add(painelChecks, gbc);
        
        // --- PAINEL DE AÇÕES ---
        JButton btnSalvar = new JButton("Salvar e Fechar");
        JButton btnCancelar = new JButton("Cancelar");
        JPanel painelAcoes = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        painelAcoes.add(btnSalvar); painelAcoes.add(btnCancelar);
        gbc.gridy = 9; gbc.gridwidth = 3; add(painelAcoes, gbc);

        // --- AÇÕES DOS COMPONENTES ---
        seletorJogador.addActionListener(e -> carregarDadosDoJogador());
        btnAplicarPercentual.addActionListener(e -> aplicarAumentoPercentual());
        btnSalvar.addActionListener(e -> salvarAlteracoes());
        btnCancelar.addActionListener(e -> dispose());
        
        carregarDadosDoJogador();
    }

    private void carregarDadosDoJogador() {
        String selecionado = (String) seletorJogador.getSelectedItem();
        boolean todosSelecionados = "Todos os Jogadores".equals(selecionado);

        // Habilita/desabilita campos de edição
        txtHp.setEnabled(!todosSelecionados);
        txtAtk.setEnabled(!todosSelecionados);
        txtEnergia.setEnabled(!todosSelecionados);
        txtVelocidade.setEnabled(!todosSelecionados);
        txtFormaPontos.setEnabled(!todosSelecionados);
        btnAplicarPercentual.setEnabled(!todosSelecionados);

        if (todosSelecionados) {
            limparCampos();
            return;
        }

        listaJogadores.stream()
            .filter(j -> j.getNome().equals(selecionado))
            .findFirst()
            .ifPresent(j -> {
                txtHp.setText(j.getHpBase().toString());
                txtAtk.setText(j.getAtk().toString());
                txtEnergia.setText(j.getEnergiaBase().toString());
                txtVelocidade.setText(j.getVelocidade().toString());
                txtFormaPontos.setText(j.getFormaPontosBase().toString());
            });
    }

    private void limparCampos() {
        txtHp.setText("");
        txtAtk.setText("");
        txtEnergia.setText("");
        txtVelocidade.setText("");
        txtFormaPontos.setText("");
    }

    private void aplicarAumentoPercentual() {
        try {
            double percentual = Double.parseDouble(txtPercentual.getText().trim());
            if (percentual <= 0) {
                JOptionPane.showMessageDialog(this, "O percentual deve ser positivo.", "Erro", JOptionPane.ERROR_MESSAGE);
                return;
            }

            BigDecimal multiplicador = BigDecimal.ONE.add(BigDecimal.valueOf(percentual / 100.0));

            if (chkHp.isSelected()) txtHp.setText(new BigDecimal(txtHp.getText()).multiply(multiplicador).toBigInteger().toString());
            if (chkAtk.isSelected()) txtAtk.setText(new BigDecimal(txtAtk.getText()).multiply(multiplicador).toBigInteger().toString());
            if (chkEnergia.isSelected() && new BigInteger(txtEnergia.getText()).signum() >= 0) {
                txtEnergia.setText(new BigDecimal(txtEnergia.getText()).multiply(multiplicador).toBigInteger().toString());
                
            }
            if (chkVelocidade.isSelected()) txtVelocidade.setText(new BigDecimal(txtVelocidade.getText()).multiply(multiplicador).toBigInteger().toString());
            
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Insira valores numéricos válidos nos campos.", "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void salvarAlteracoes() {
        try {
            Path caminho = Paths.get(caminhoArquivo);
            List<String> linhas = Files.readAllLines(caminho, StandardCharsets.UTF_8);
            List<String> novasLinhas = new ArrayList<>();
            String selecionado = (String) seletorJogador.getSelectedItem();
            
            boolean paraTodos = "Todos os Jogadores".equals(selecionado);
            
            for (String linha : linhas) {
                if (linha.trim().isEmpty()) {
                    novasLinhas.add(linha);
                    continue;
                }
                String[] partes = linha.split(";", -1);
                String nomeNaLinha = partes[0];
                
                if (paraTodos || nomeNaLinha.equals(selecionado)) {
                    BigInteger hp, atk, energia, vel, fp;
                    
                    if (paraTodos) {
                        double percentual = Double.parseDouble(txtPercentual.getText().trim());
                        BigDecimal multiplicador = BigDecimal.ONE.add(BigDecimal.valueOf(percentual / 100.0));
                        
                        hp = new BigInteger(partes[1]);
                        atk = new BigInteger(partes[2]);
                        energia = new BigInteger(partes[3]);
                        vel = new BigInteger(partes[4]);
                        fp = new BigInteger(partes[5]);
                        
                        if(chkHp.isSelected()) hp = new BigDecimal(hp).multiply(multiplicador).toBigInteger();
                        if(chkAtk.isSelected()) atk = new BigDecimal(atk).multiply(multiplicador).toBigInteger();
                        if(chkEnergia.isSelected() && energia.signum() >= 0) energia = new BigDecimal(energia).multiply(multiplicador).toBigInteger();
                        if(chkVelocidade.isSelected()) vel = new BigDecimal(vel).multiply(multiplicador).toBigInteger();
 
                    } else {
                        hp = new BigInteger(txtHp.getText());
                        atk = new BigInteger(txtAtk.getText());
                        energia = new BigInteger(txtEnergia.getText());
                        vel = new BigInteger(txtVelocidade.getText());
                        fp = new BigInteger(txtFormaPontos.getText());
                    }
                    
                    String novaLinha = String.join(";", nomeNaLinha, hp.toString(), atk.toString(),
                            energia.toString(), vel.toString(), fp.toString(),
                            partes[6], partes[7], partes[8], partes[9], partes[10]);
                    novasLinhas.add(novaLinha);
                } else {
                    novasLinhas.add(linha);
                }
            }
            Files.write(caminho, novasLinhas, StandardCharsets.UTF_8);
            owner.recarregarDadosEAtualizarTabela();
            dispose();

        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Erro ao salvar arquivo: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        } catch (NumberFormatException e) {
             JOptionPane.showMessageDialog(this, "Todos os campos de texto e percentual devem conter números válidos.", "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }
}