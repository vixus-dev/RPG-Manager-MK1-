package com.Vixus.inc;

import javax.swing.*;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Random;

public class GirarDado extends JFrame {

    private JTextField campoQuantidadeDados;
    private JTextField campoNumeroFaces;
    private JTextArea areaResultados;
    private JLabel labelSomaTotal;
    private JTabbedPane tabbedPane;

    public GirarDado() {
    	//criando a janela
        setTitle("Rolador de dados 2000");
        setSize(400, 250);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        tabbedPane = new JTabbedPane();

     // Aba 1
        JPanel aba1 = new JPanel();
        aba1.setLayout(new BoxLayout(aba1, BoxLayout.Y_AXIS));
        aba1.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20)); // Espaçamento interno
        aba1.setBackground(Color.LIGHT_GRAY); // Exemplo de fundo

        campoQuantidadeDados = new JTextField();
        campoNumeroFaces = new JTextField();
        
        // Campos de entrada
        aba1.add(new JLabel("Quantidade de Dados:"));
        aba1.add(campoQuantidadeDados);
        aba1.add(Box.createVerticalStrut(10));

        aba1.add(new JLabel("Número de Faces por Dado:"));
        aba1.add(campoNumeroFaces);
        aba1.add(Box.createVerticalStrut(20));

        // Botão dentro de um painel para centralização
        JButton botaoRolar = new JButton("Rolar Dados");
        JPanel painelBotao = new JPanel(new FlowLayout(FlowLayout.CENTER));
        painelBotao.setOpaque(false); // Deixa o fundo transparente para combinar
        painelBotao.add(botaoRolar);

        aba1.add(painelBotao);

        // Aba 2 - Resultados Individuais
        JPanel Aba2 = new JPanel(new BorderLayout());
        areaResultados = new JTextArea();
        areaResultados.setEditable(false);
        JScrollPane scroll = new JScrollPane(areaResultados);
        Aba2.add(scroll, BorderLayout.CENTER);

        // Aba 3 - Soma Total
        JPanel Aba3 = new JPanel(new FlowLayout());
        labelSomaTotal = new JLabel("Dano total Causado: ");
        Aba3.add(labelSomaTotal);

        // Adiciona abas ao tabbedPane
        tabbedPane.addTab("Configurar Dados", aba1);
        tabbedPane.addTab("Resultados Individuais", Aba2);
        tabbedPane.addTab("Dano Causado", Aba3);

        add(tabbedPane);

        // Ação do botão
        botaoRolar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                rolarDados();
            }
        });

        setVisible(true);
    }

    private void rolarDados() {
        try {
            long quantidade = Integer.parseInt(campoQuantidadeDados.getText());
            long faces = Integer.parseInt(campoNumeroFaces.getText());

            if (quantidade <= 0 || faces <= 0) {
                JOptionPane.showMessageDialog(this, "Insira valores positivos.", "Erro", JOptionPane.ERROR_MESSAGE);
                return;
            }

            Random random = new Random();
            StringBuilder resultados = new StringBuilder("Resultados da rolagem:\n");
            long somaTotal = 0;
            Boolean acerta;
            
            for (int i = 1; i <= quantidade; i++) {
                Long resultado = (random.nextLong(faces) + 1);
                if(resultado > (faces * 0.2)) {
                	acerta = true;
                } else {
					acerta = false;
				}
                if (acerta) {
                    somaTotal += resultado;
                }

                resultados.append("Dado ").append(i).append(": ").append(resultado)
                         .append(acerta ? " ✅ Acertou" : " ❌ Errou").append("\n");
            }

            // Atualiza abas
            areaResultados.setText(resultados.toString());
            labelSomaTotal.setText("Dano total Causado: " + somaTotal);

            // Muda para aba de resultados individuais
            tabbedPane.setSelectedIndex(1);

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Digite apenas números inteiros válidos.", "Erro de Entrada", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new GirarDado());
    }
}