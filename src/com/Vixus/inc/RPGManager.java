package com.Vixus.inc;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.awt.event.KeyEvent;

public class RPGManager extends JFrame {
	
    private JTable tabelaAtributos;
    private DefaultTableModel modeloTabela;

    private java.util.List<Jogador> listaJogadores = new ArrayList<>();
    private java.util.List<Inimigo> listaInimigos = new ArrayList<>();
    private java.util.List<Transformacao> listaTransformacoes = new ArrayList<>();
    private java.util.List<Ampliacao> listaAmpliacoes = new ArrayList<>();
    private java.util.List<Tecnica> listaTecnicas = new ArrayList<>();

    public RPGManager() {
        setTitle("RPG Manager");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(900, 600);
        setLocationRelativeTo(null);

        JPanel painel = criarPainelUnificado();
        add(painel);

        importarDados();
        atualizarTabela();
    }

    private JPanel criarPainelUnificado() {
        JPanel painel = new JPanel(new BorderLayout());

        modeloTabela = new DefaultTableModel(
                new String[]{"Tipo", "Nome", "HP", "ATK", "Energia/Bateria", "Velocidade", "Transformação", "Ampliação", "Pontos Forma"}, 0);
        tabelaAtributos = new JTableComTooltips(modeloTabela, this);
        painel.add(new JScrollPane(tabelaAtributos), BorderLayout.CENTER);

        JPanel painelBotoes = new JPanel();

        JButton btnIniciarTurno = new JButton("Iniciar Turno");
        JButton btnTransformar = new JButton("Transformar ou Ampliar");
        JButton btnGerenciarInimigos = new JButton("Gerenciar Inimigos");
        JButton btnInventario = new JButton("Abrir Inventário");
        JButton btnFusões = new JButton("Gerenciar Fusões");
        JButton btnEditarPersonagens = new JButton("Editor de Personagens");
        
        // --- Configuração dos Atalhos (SLA oq é pra fazer peguei da IA) ---
        btnIniciarTurno.setMnemonic(KeyEvent.VK_1);      // ALT + 1
        btnTransformar.setMnemonic(KeyEvent.VK_2);      // ALT + 2
        btnGerenciarInimigos.setMnemonic(KeyEvent.VK_3);  // ALT + 3
        btnInventario.setMnemonic(KeyEvent.VK_4);       // ALT + 4
        btnFusões.setMnemonic(KeyEvent.VK_5);           // ALT + 5
        btnEditarPersonagens.setMnemonic(KeyEvent.VK_6);  // ALT + 6

        //função dos botões
        btnIniciarTurno.addActionListener(e -> abrirPopupTurno());
        btnTransformar.addActionListener(e -> abrirPopupTransformar());
        btnGerenciarInimigos.addActionListener(e -> abrirPopupGerenciarInimigos());
        btnInventario.addActionListener(e -> abrirPopupInventario());
        btnFusões.addActionListener(e -> abrirPopupFusoes());
        btnEditarPersonagens.addActionListener(e -> {
            // A ação de criar e mostrar a janela acontece AQUI DENTRO
            new EditorDePersonagens(this, listaJogadores).setVisible(true);
        });

        painelBotoes.add(btnIniciarTurno);
        painelBotoes.add(btnTransformar);
        painelBotoes.add(btnGerenciarInimigos);
        painelBotoes.add(btnInventario);
        painelBotoes.add(btnFusões);
        painelBotoes.add(btnEditarPersonagens);

        painel.add(painelBotoes, BorderLayout.SOUTH);
        return painel;
    }
    
    private static String formatarNumero(BigInteger numero) {
        if (numero == null) return "0";
        if (numero.compareTo(new BigInteger("1000")) < 0) {
            return numero.toString(); // Retorna o número normal se for menor que 1000
        }

        // TreeMap para manter os divisores e sufixos em ordem
        TreeMap<BigInteger, String> map = new TreeMap<>();
        map.put(new BigInteger("1000"), "K");
        map.put(new BigInteger("1000000"), "M");
        map.put(new BigInteger("1000000000"), "B");
        map.put(new BigInteger("1000000000000"), "T");
        map.put(new BigInteger("1000000000000000"), "Qa");
        map.put(new BigInteger("1000000000000000000"), "Qt");
        map.put(new BigInteger("1000000000000000000000"), "Sx");
        map.put(new BigInteger("1000000000000000000000000"), "Sp");
        map.put(new BigInteger("1000000000000000000000000000"), "Oc");
        map.put(new BigInteger("1000000000000000000000000000000"), "No");
        map.put(new BigInteger("1000000000000000000000000000000000"), "Dc");
        
        // Encontra o divisor apropriado
        Map.Entry<BigInteger, String> entry = map.floorEntry(numero);
        BigInteger divisor = entry.getKey();
        String sufixo = entry.getValue();

        // Faz o cálculo com uma casa decimal
        BigInteger parteInteira = numero.divide(divisor);
        BigInteger resto = numero.remainder(divisor);
        BigInteger parteDecimal = resto.divide(divisor.divide(BigInteger.TEN));

        if (parteDecimal.equals(BigInteger.ZERO)) {
            return parteInteira + sufixo;
        } else {
            return parteInteira + "." + parteDecimal + sufixo;
        }
    }
    

    private void importarDados() {
        importarJogadores("src/com/Vixus/inc/resources/jogadores.txt");
        importarInimigos("src/com/Vixus/inc/resources/inimigos.txt");
        importarTransformacoes("src/com/Vixus/inc/resources/transformacoes.txt");
        importarAmpliacoes("src/com/Vixus/inc/resources/ampliacoes.txt");
        importarTecnicas("src/com/Vixus/inc/resources/tecnicas.txt");
    }

    private void importarJogadores(String caminho) {
        listaJogadores.clear();
        try (BufferedReader br = new BufferedReader(new FileReader(caminho))) {
            String linha;
            while ((linha = br.readLine()) != null) {
                if (linha.trim().isEmpty()) continue;
                Jogador j = Jogador.fromLinha(linha);
                listaJogadores.add(j);
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Erro ao importar jogadores: " + e.getMessage());
        }
    }
    
    private void importarInimigos(String caminho) {
        listaInimigos.clear();
        try (BufferedReader br = new BufferedReader(new FileReader(caminho))) {
            String linha;
            while ((linha = br.readLine()) != null) {
                if (linha.trim().isEmpty()) continue;
                Inimigo i = Inimigo.fromLinha(linha);
                if (i != null) {
                    listaInimigos.add(i);
                }
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Erro ao importar inimigos: " + e.getMessage());
        }
    }

    private void importarTransformacoes(String caminho) {
        listaTransformacoes.clear();
        try (BufferedReader br = new BufferedReader(new FileReader(caminho))) {
            String linha;
            while ((linha = br.readLine()) != null) {
                if (linha.trim().isEmpty()) continue;
                Transformacao t = Transformacao.fromLinha(linha);
                listaTransformacoes.add(t);
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Erro ao importar transformações: " + e.getMessage());
        }
    }

    private void importarAmpliacoes(String caminho) {
        listaAmpliacoes.clear();
        try (BufferedReader br = new BufferedReader(new FileReader(caminho))) {
            String linha;
            while ((linha = br.readLine()) != null) {
                if (linha.trim().isEmpty()) continue;
                Ampliacao a = Ampliacao.fromLinha(linha);
                listaAmpliacoes.add(a);
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Erro ao importar ampliações: " + e.getMessage());
        }
    }

    private void atualizarTabela() {
        modeloTabela.setRowCount(0);
        for (Jogador j : listaJogadores) {
            String nomeTransformacao = (j.getTransformacao() != null) ? j.getTransformacao().getNome() : "-";
            String nomeAmpliacao = (j.getAmpliacao() != null) ? j.getAmpliacao().getNome() : "-";

            // Decide se mostra Energia ou Bateria
            String displayEnergiaBateria;
            if (j.isAndroide()) {
                displayEnergiaBateria = String.format("%.1f%%", j.getBateria()); // Mostra bateria com uma casa decimal
            } else {
                displayEnergiaBateria = formatarNumero(j.getEnergia());
            }

            String displayForma = j.isFormaIlimitada() ? "∞" : formatarNumero(j.getFormaPontos());

            modeloTabela.addRow(new Object[]{
                "Jogador", j.getNome(),
                formatarNumero(j.getHP()), formatarNumero(j.getAtk()),
                displayEnergiaBateria, // Usa a string decidida acima
                formatarNumero(j.getVelocidade()),
                nomeTransformacao, nomeAmpliacao,
                displayForma
            });
        }
        for (Inimigo i : listaInimigos) {
            // Lógica para pegar nomes de trans/amp do inimigo
            String nomeTransformacao = (i.getTransformacao() != null) ? i.getTransformacao().getNome() : "-";
            String nomeAmpliacao = (i.getAmpliacao() != null) ? i.getAmpliacao().getNome() : "-";

            modeloTabela.addRow(new Object[]{
                "Inimigo",
                i.getNome(),
                formatarNumero(i.getHp()),
                formatarNumero(i.getAtk()),
                formatarNumero(i.getEnergia()),
                formatarNumero(i.getVelocidade()),
                nomeTransformacao, // Mostra a transformação
                nomeAmpliacao,   // Mostra a ampliação
                formatarNumero(i.getFormaPontos()) // Mostra os pontos de forma
            });
        }
    }

    private void abrirPopupTurno() {
        JDialog popup = new JDialog(this, "Iniciar Turno", true);
        popup.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JComboBox<String> comboAtacante = new JComboBox<>();
        JComboBox<String> comboAlvo = new JComboBox<>();
        JComboBox<Tecnica> comboTecnica = new JComboBox<>(listaTecnicas.toArray(new Tecnica[0]));
        
        // Populando os combos
        for (Jogador j : listaJogadores) comboAtacante.addItem(j.getNome() + " (Jogador)");
        for (Inimigo i : listaInimigos) comboAtacante.addItem(i.getNome() + " (Inimigo)");
        for (Jogador j : listaJogadores) comboAlvo.addItem(j.getNome() + " (Jogador)");
        for (Inimigo i : listaInimigos) comboAlvo.addItem(i.getNome() + " (Inimigo)");

        comboTecnica.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Tecnica t) {
                    setText(t.getNome());
                    setToolTipText("Custo: " + t.getCusto() + " | Dano: " + t.getMultiplicadorTexto());
                }
                return this;
            }
        });

        JTextField campoEnergia = new JTextField(5);
        JTextField campoDanoPrevisto = new JTextField(10);
        campoDanoPrevisto.setEditable(true); // Permitir edição manual

        final boolean[] danoManualEditado = {false};

        DocumentListener docListener = new DocumentListener() {
            public void update() {
                if (!danoManualEditado[0]) {
                    atualizarDanoCalculado(comboAtacante, campoEnergia, comboTecnica, campoDanoPrevisto);
                }
            }
            public void insertUpdate(DocumentEvent e) { update(); }
            public void removeUpdate(DocumentEvent e) { update(); }
            public void changedUpdate(DocumentEvent e) { update(); }
        };

        ActionListener recalculateListener = e -> {
            danoManualEditado[0] = false;
            docListener.insertUpdate(null);
        };
        
        comboAtacante.addActionListener(recalculateListener);
        comboAlvo.addActionListener(recalculateListener);
        comboTecnica.addActionListener(recalculateListener);
        campoEnergia.getDocument().addDocumentListener(docListener);

        campoDanoPrevisto.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                danoManualEditado[0] = true;
            }
        });
        
        // Layout
        gbc.gridx = 0; gbc.gridy = 0; popup.add(new JLabel("Atacante:"), gbc);
        gbc.gridx = 1; popup.add(comboAtacante, gbc);
        gbc.gridx = 0; gbc.gridy = 1; popup.add(new JLabel("Alvo:"), gbc);
        gbc.gridx = 1; popup.add(comboAlvo, gbc);
        gbc.gridx = 0; gbc.gridy = 2; popup.add(new JLabel("Técnica:"), gbc);
        gbc.gridx = 1; popup.add(comboTecnica, gbc);
        gbc.gridx = 0; gbc.gridy = 3; popup.add(new JLabel("Energia usada:"), gbc);
        gbc.gridx = 1; popup.add(campoEnergia, gbc);
        gbc.gridx = 0; gbc.gridy = 4; popup.add(new JLabel("Dano Previsto:"), gbc);
        gbc.gridx = 1; popup.add(campoDanoPrevisto, gbc);

        JButton btnConfirmar = new JButton("Executar ataque");
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2; popup.add(btnConfirmar, gbc);
        JButton btnPassarTurno = new JButton("Passar Turno");
        gbc.gridx = 0; gbc.gridy = 6; gbc.gridwidth = 2; popup.add(btnPassarTurno, gbc);

        // --- LÓGICA DE ATAQUE ---
        btnConfirmar.addActionListener(e -> {
            String attSel = (String) comboAtacante.getSelectedItem();
            String alvoSel = (String) comboAlvo.getSelectedItem();
            Tecnica t = (Tecnica) comboTecnica.getSelectedItem();
            
            if (attSel == null || alvoSel == null || t == null) {
                JOptionPane.showMessageDialog(popup, "Escolha atacante, alvo e técnica.");
                return;
            }

            try {
                BigInteger danoTotal = new BigInteger(campoDanoPrevisto.getText().trim());
                String textoInput = campoEnergia.getText().trim();
                if (textoInput.isEmpty()) {
                    JOptionPane.showMessageDialog(popup, "Insira um valor de energia ou número de ataques.");
                    return;
                }
                
                Object atacante = buscarPersonagemPorNomeComTipo(attSel);
                Object alvo = buscarPersonagemPorNomeComTipo(alvoSel);
                
                if (atacante instanceof Jogador jAtt) {
                    if (!(alvo instanceof Inimigo iAlvo)) {
                        JOptionPane.showMessageDialog(popup, "Jogadores só podem atacar inimigos.");
                        return;
                    }

                    if (jAtt.isAndroide()) {
                        int numeroDeAcoes = Integer.parseInt(textoInput);
                        if(numeroDeAcoes <= 0) { /* ... validação ... */ return; }
                        
                        // Lógica de ULTRADRIVE
                        if (jAtt.isInUltradrive()) {
                            double bateriaGasta = numeroDeAcoes * 3.0;
                            jAtt.setBateria(jAtt.getBateria() - bateriaGasta);
                            iAlvo.setHp(iAlvo.getHp().subtract(danoTotal));
                            JOptionPane.showMessageDialog(this, "ULTRADRIVE! (" + numeroDeAcoes + "x) Causa " + formatarNumero(danoTotal) + " de dano! Custo: " + bateriaGasta + "%");
                        }
                        // Lógica de OVERDRIVE
                        else if (jAtt.getBateria() <= 0) {
                            double bateriaGasta = numeroDeAcoes * 1.0;
                            jAtt.setBateria(jAtt.getBateria() - bateriaGasta);
                            iAlvo.setHp(iAlvo.getHp().subtract(danoTotal));
                            JOptionPane.showMessageDialog(this, "OVERDRIVE! (" + numeroDeAcoes + "x) Causa " + formatarNumero(danoTotal) + " de dano! Custo: " + bateriaGasta + "%");
                        }
                        // Lógica NORMAL
                        else {
                            BigInteger energiaGasta = new BigInteger(textoInput);
                            if (energiaGasta.doubleValue() > jAtt.getEnergiaEquivalente()) {
                                JOptionPane.showMessageDialog(popup, "Bateria insuficiente!");
                                return;
                            }
                            double bateriaGasta = energiaGasta.doubleValue() * Jogador.PONTOS_ENERGIA_POR_BATERIA;
                            jAtt.setBateria(jAtt.getBateria() - bateriaGasta);
                            iAlvo.setHp(iAlvo.getHp().subtract(danoTotal));
                            JOptionPane.showMessageDialog(this, jAtt.getNome() + " atacou causando " + formatarNumero(danoTotal) + " de dano!");
                        }
                        
                   // LÓGICA DE ENERGIA PARA OUTRAS RAÇAS
                    } else {
                        BigInteger energiaGasta = new BigInteger(textoInput);
                        if (jAtt.getEnergia().compareTo(energiaGasta) < 0) {
                            JOptionPane.showMessageDialog(popup, jAtt.getNome() + " não tem energia suficiente!");
                            return;
                        }
                        jAtt.setEnergia(jAtt.getEnergia().subtract(energiaGasta));
                        iAlvo.setHp(iAlvo.getHp().subtract(danoTotal));
                        JOptionPane.showMessageDialog(this,
                            jAtt.getNome() + " atacou causando " + formatarNumero(danoTotal) + " de dano! Energia gasta: " + formatarNumero(energiaGasta));
                    }
                    
                    finalizarTurno(jAtt);

                } else if (atacante instanceof Inimigo iAtt) {
                    // 1. Validação: Garante que o alvo do inimigo é um jogador
                    if (!(alvo instanceof Jogador jAlvo)) {
                        JOptionPane.showMessageDialog(popup, "Inimigos só podem atacar jogadores.");
                        return;
                    }

                    // 2. Leitura dos Inputs: Pega a energia gasta e o dano total dos campos de texto
                    BigInteger energiaGasta;
                    try {
                        energiaGasta = new BigInteger(campoEnergia.getText().trim());
                        if(energiaGasta.signum() < 0) throw new NumberFormatException(); // Impede energia negativa
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(popup, "Energia inválida para o ataque do inimigo.");
                        return;
                    }

                    // 3. Validação de Custo: Verifica se a energia gasta é suficiente para o custo da técnica
                    BigInteger custoTecnica = BigInteger.valueOf(t.getCusto());
                    if (custoTecnica.compareTo(BigInteger.ZERO) > 0 && energiaGasta.compareTo(custoTecnica) < 0) {
                        JOptionPane.showMessageDialog(popup, iAtt.getNome() + " precisa de pelo menos " + t.getCusto() + " de energia para usar " + t.getNome() + ".");
                        return;
                    }
                    
                    // 4. Validação de Recursos: Verifica se o inimigo possui a energia necessária para gastar
                    if (iAtt.getEnergia().compareTo(energiaGasta) < 0) {
                        JOptionPane.showMessageDialog(popup, iAtt.getNome() + " não tem energia suficiente!");
                        return;
                    }
                    
                    // 5. Aplicação das Mudanças: Subtrai a energia do inimigo e o HP do jogador alvo
                    iAtt.setEnergia(iAtt.getEnergia().subtract(energiaGasta));
                    jAlvo.setHp(jAlvo.getHP().subtract(danoTotal));

                    // 6. Feedback ao Usuário: Exibe o resultado do ataque em uma janela de diálogo
                    JOptionPane.showMessageDialog(this,
                        iAtt.getNome() + " atacou " + jAlvo.getNome() + " usando " + t.getNome() + "\n" +
                        "causando " + formatarNumero(danoTotal) + " de dano! Energia gasta: " + formatarNumero(energiaGasta));
                    
                    // 7. Fim do Turno: Chama o método para finalizar o turno do inimigo
                    // (Isso deduz os Pontos de Forma se ele estiver transformado)
                    finalizarTurno(iAtt);
                }
                
                atualizarTabela();
                popup.dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(popup, "Ocorreu um erro: " + ex.getMessage());
                ex.printStackTrace();
            }
        });
        btnPassarTurno.addActionListener(e -> {
            String attSel = (String) comboAtacante.getSelectedItem();
            if (attSel == null) {
                JOptionPane.showMessageDialog(popup, "Selecione um personagem para passar o turno.");
                return;
            }

            Object personagem = buscarPersonagemPorNomeComTipo(attSel);

            // Lógica para Jogador
            if (personagem instanceof Jogador j) {
                // Recuperação de Energia (ignorado para Androides)
                if (!j.isAndroide()) {
                    BigInteger energiaMax = j.getEnergiaBase();
                    BigInteger recuperacaoEnergia = new BigDecimal(energiaMax).multiply(new BigDecimal("0.10")).toBigInteger();
                    BigInteger novaEnergia = j.getEnergia().add(recuperacaoEnergia);
                    // Garante que não ultrapasse o máximo
                    if (novaEnergia.compareTo(energiaMax) > 0) {
                        novaEnergia = energiaMax;
                    }
                    j.setEnergia(novaEnergia);
                }

                // Recuperação de Pontos de Forma (ignorado se for ilimitado)
                if (!j.isFormaIlimitada()) {
                    BigInteger formaMax = j.getFormaPontosBase();
                    BigInteger recuperacaoForma = new BigDecimal(formaMax).multiply(new BigDecimal("0.20")).toBigInteger();
                    BigInteger novaForma = j.getFormaPontos().add(recuperacaoForma);
                    if (novaForma.compareTo(formaMax) > 0) {
                        novaForma = formaMax;
                    }
                    j.setFormaPontos(novaForma);
                }

                JOptionPane.showMessageDialog(popup, j.getNome() + " passou o turno");
                finalizarTurno(j); // Finaliza o turno para aplicar custos de transformação
            
            // Lógica para Inimigo
            } else if (personagem instanceof Inimigo i) {
                BigInteger energiaMax = i.getEnergia();
                BigInteger recuperacaoEnergia = new BigDecimal(energiaMax).multiply(new BigDecimal("0.20")).toBigInteger();
                BigInteger novaEnergia = i.getEnergia().add(recuperacaoEnergia);
                if (novaEnergia.compareTo(energiaMax) > 0) {
                    novaEnergia = energiaMax;
                }
                i.setEnergia(novaEnergia);

                BigInteger formaMax = i.getFormaPontos();
                BigInteger recuperacaoForma = new BigDecimal(formaMax).multiply(new BigDecimal("0.10")).toBigInteger();
                BigInteger novaForma = i.getFormaPontos().add(recuperacaoForma);
                if (novaForma.compareTo(formaMax) > 0) {
                    novaForma = formaMax;
                }
                i.setFormaPontos(novaForma);

                JOptionPane.showMessageDialog(popup, i.getNome() + " passou o turno para se recompor.");
                finalizarTurno(i);
            }

            atualizarTabela();
            popup.dispose();
        });

        popup.pack();
        popup.setLocationRelativeTo(this);
        popup.setVisible(true);
        recalculateListener.actionPerformed(null); // Chama uma vez para configurar inicial
    }

    private void atualizarDanoCalculado(JComboBox<String> comboAtacante, JTextField campoEnergia,
            JComboBox<Tecnica> comboTecnica, JTextField campoDanoPrevisto) {

    	Tecnica t = (Tecnica) comboTecnica.getSelectedItem();
    	String textoInput = campoEnergia.getText().trim();
    	Object atacante = buscarPersonagemPorNomeComTipo((String) comboAtacante.getSelectedItem());

    	if (t == null || textoInput.isEmpty() || atacante == null) {
    		campoDanoPrevisto.setText("0");
    		return;
    	}

    	try {
    		if (atacante instanceof Jogador jAtt && jAtt.isAndroide()) {
    			int numeroDeAcoes = Integer.parseInt(textoInput);
    			if (numeroDeAcoes <= 0) {
    				campoDanoPrevisto.setText("0");
    				return;
    			}

    			// Custo e bônus dependem do estado do Androide
    			double custoPorAcao = (jAtt.isInUltradrive() || jAtt.getBateria() <= 0) ? 
    					(jAtt.isInUltradrive() ? 3.0 : 1.0) : 
    						(t.getCusto() / Jogador.PONTOS_ENERGIA_POR_BATERIA);

    			BigInteger danoDeUmaAcao = t.rolarDano(jAtt.getAtk());
    			BigInteger danoTotalPrevisto = danoDeUmaAcao.multiply(BigInteger.valueOf(numeroDeAcoes));
    			campoDanoPrevisto.setText(danoTotalPrevisto.toString());

    		} else {
    			// Lógica para não-Androides
    			BigInteger energiaGasta = new BigInteger(textoInput);
    			BigInteger custoTecnica = BigInteger.valueOf(t.getCusto());
    			if (energiaGasta.compareTo(BigInteger.ZERO) < 0 || custoTecnica.compareTo(BigInteger.ZERO) <= 0) {
    				campoDanoPrevisto.setText("0");
    				return;
    			}
    			BigInteger vezes = energiaGasta.divide(custoTecnica);
    			if (vezes.compareTo(BigInteger.ZERO) <= 0) {
    				campoDanoPrevisto.setText("0");
    				return;
    			}
    			BigInteger atkBaseParaCalculo = (atacante instanceof Jogador) ? ((Jogador) atacante).getAtk() : ((Inimigo) atacante).getAtk();
    			BigInteger danoTotal = BigInteger.ZERO;
    			for (BigInteger i = BigInteger.ZERO; i.compareTo(vezes) < 0; i = i.add(BigInteger.ONE)) {
    				danoTotal = danoTotal.add(t.rolarDano(atkBaseParaCalculo));
    			}
    			campoDanoPrevisto.setText(danoTotal.toString());
    		}
    	} catch (NumberFormatException | ArithmeticException ex) {
    		campoDanoPrevisto.setText("0");
    	}
    }


	private Object buscarPersonagemPorNomeComTipo(String nomeTipo) {
        // Exemplo: "Goku (Jogador)" ou "Saibaman (Inimigo)"
        if (nomeTipo.endsWith("(Jogador)")) {
            String nome = nomeTipo.replace(" (Jogador)", "");
            for (Jogador j : listaJogadores) {
                if (j.getNome().equals(nome)) return j;
            }
        } else if (nomeTipo.endsWith("(Inimigo)")) {
            String nome = nomeTipo.replace(" (Inimigo)", "");
            for (Inimigo i : listaInimigos) {
                if (i.getNome().equals(nome)) return i;
            }
        }
        return null;
    }
    
	private void finalizarTurno(Jogador j) {
        if (j.getTransformacao() != null) {
            j.reduzirFormaPontos(j.getTransformacao().getCustoPorTurno());
        }
        if (j.getAmpliacao() != null) {
            j.reduzirFormaPontos(j.getAmpliacao().getCustoPorTurno());
        }
        // Compara o BigInteger com zero usando o método .compareTo()
        if (j.getFormaPontos().compareTo(BigInteger.ZERO) <= 0 && j.formaIlimitada != true) {
            if (j.getTransformacao() != null && j.getTransformacao().getCustoPorTurno() != 0) {
                j.destransformar();
                JOptionPane.showMessageDialog(this, "A energia de " + j.getNome() + " se esgotou e se destransformou.");
            }
            if (j.getAmpliacao() != null && j.getAmpliacao().getCustoPorTurno() != 0) {
                j.desampliar();
                JOptionPane.showMessageDialog(this, "A energia de " + j.getNome() + " se esgotou e retornou à forma base.");
            }
        }
        
        if (j.isAndroide()) {
            //ENTRAR EM ULTRADRIVE
            if (!j.isInUltradrive() && j.getBateria() <= -25.0) {
                j.setInUltradrive(true);
                j.setBateria(0.0);
                JOptionPane.showMessageDialog(this, j.getNome() + " Está Sobrecarregada!\nULTRADRIVE ATIVADO! Bateria retornou para 0%.");
            }
            //SAIR DO ULTRADRIVE
            else if (j.isInUltradrive() && j.getBateria() <= -30.0) {
                j.setInUltradrive(false);
                JOptionPane.showMessageDialog(this, j.getNome() + " entrou em MeltDown!\nSTUN recebido! Retornando ao modo Overdrive.");
            }
        }
        
        j.atualizarAtributos();
        atualizarTabela(); // É uma boa ideia atualizar a tabela aqui também para refletir a mudança
    }
	
	private void finalizarTurno(Inimigo i) {
	    if (i.getTransformacao() != null) {
	        i.reduzirFormaPontos(i.getTransformacao().getCustoPorTurno());
	    }
	    if (i.getAmpliacao() != null) {
	        i.reduzirFormaPontos(i.getAmpliacao().getCustoPorTurno());
	    }

	    if (i.getFormaPontos().compareTo(BigInteger.ZERO) <= 0) {
	        if (i.getTransformacao() != null) {
	            i.destransformar();
	        }
	        if (i.getAmpliacao() != null) {
	            i.desampliar();
	        }
	        JOptionPane.showMessageDialog(this, "A energia de " + i.getNome() + " se esgotou e retornou à forma base.");
	    }

	    i.atualizarAtributos();
	    atualizarTabela();
	}
    
	 private void abrirPopupTransformar() {
	        JDialog popup = new JDialog(this, "Transformar ou Ampliar", true);
	        popup.setLayout(new GridLayout(5, 2, 5, 5));

	        JComboBox<String> comboTipo = new JComboBox<>(new String[]{"Transformação", "Ampliação"});
	        JComboBox<String> comboAlvo = new JComboBox<>(); // Agora vai ter Jogadores e Inimigos
	        JComboBox<String> comboForma = new JComboBox<>();

	        // Preenche o combo de alvos com jogadores e inimigos
	        for (Jogador j : listaJogadores) comboAlvo.addItem(j.getNome() + " (Jogador)");
	        for (Inimigo i : listaInimigos) comboAlvo.addItem(i.getNome() + " (Inimigo)");

	        comboTipo.addActionListener(e -> {
	            comboForma.removeAllItems();
	            if (comboTipo.getSelectedItem().equals("Transformação")) {
	                for (Transformacao t : listaTransformacoes) comboForma.addItem(t.getNome());
	            } else {
	                for (Ampliacao a : listaAmpliacoes) comboForma.addItem(a.getNome());
	            }
	        });
	        comboTipo.setSelectedIndex(0);

	        popup.add(new JLabel("Tipo:"));
	        popup.add(comboTipo);
	        popup.add(new JLabel("Alvo:"));
	        popup.add(comboAlvo);
	        popup.add(new JLabel("Forma:"));
	        popup.add(comboForma);

	        JButton btnConfirmar = new JButton("Confirmar");
	        btnConfirmar.addActionListener(e -> {
	            String tipo = (String) comboTipo.getSelectedItem();
	            String alvoNomeComTipo = (String) comboAlvo.getSelectedItem(); // Ex: "Goku (Jogador)"
	            String formaNome = (String) comboForma.getSelectedItem();

	            if (alvoNomeComTipo == null || formaNome == null) {
	                JOptionPane.showMessageDialog(popup, "Selecione alvo e forma.");
	                return;
	            }
	            
	            // Usamos o método que busca qualquer tipo de personagem
	            Object alvo = buscarPersonagemPorNomeComTipo(alvoNomeComTipo);
	            
	            aplicarTransformacaoOuAmpliacao(tipo, alvo, formaNome); // Passamos o objeto alvo
	            
	            JOptionPane.showMessageDialog(popup, alvoNomeComTipo.split(" \\(")[0] + " se transformou em " + formaNome);
	            atualizarTabela();
	            popup.dispose();
	        });

	        JButton btnDestransformar = new JButton("Destransformar");
	        btnDestransformar.addActionListener(e -> {
	             String alvoNomeComTipo = (String) comboAlvo.getSelectedItem();
	             Object alvo = buscarPersonagemPorNomeComTipo(alvoNomeComTipo);
	             if (alvo instanceof Jogador j) {
	                 j.destransformar();
	                 JOptionPane.showMessageDialog(popup, j.getNome() + " retornou a forma base.");
	             } else if (alvo instanceof Inimigo i) {
	                 i.destransformar();
	                 JOptionPane.showMessageDialog(popup, i.getNome() + " retornou a forma base.");
	             }
	             atualizarTabela();
	        });
	        
	        JButton btnDesampliar = new JButton("Desampliar");
	        btnDesampliar.addActionListener(e -> {
	             String alvoNomeComTipo = (String) comboAlvo.getSelectedItem();
	             Object alvo = buscarPersonagemPorNomeComTipo(alvoNomeComTipo);
	             if (alvo instanceof Jogador j) {
	                 j.desampliar();
	                 JOptionPane.showMessageDialog(popup, j.getNome() + " voltou ao normal.");
	             } else if (alvo instanceof Inimigo i) {
	                 i.desampliar();
	                 JOptionPane.showMessageDialog(popup, i.getNome() + " voltou ao normal.");
	             }
	             atualizarTabela();
	        });

	        JButton btnCancelar = new JButton("Cancelar");
	        btnCancelar.addActionListener(e -> popup.dispose());

	        JPanel painelBotoes = new JPanel();
	        painelBotoes.add(btnConfirmar);
	        painelBotoes.add(btnDestransformar);
	        painelBotoes.add(btnDesampliar);
	        painelBotoes.add(btnCancelar);

	        popup.add(new JLabel()); // Espaçador
	        popup.add(painelBotoes);

	        popup.pack();
	        popup.setLocationRelativeTo(this);
	        popup.setVisible(true);
	    }
    
    private void abrirPopupInventario() {
        JDialog dialog = new JDialog(this, "Inventário", true);
        dialog.setSize(500, 400);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JComboBox<Jogador> jogadorCombo = new JComboBox<>(listaJogadores.toArray(new Jogador[0]));
        JPanel painelItens = new JPanel(new GridLayout(0, 1, 5, 5)); // Lista vertical

        // Carregar itens disponíveis do arquivo itens.txt
        java.util.List<String> itensDisponiveis = new ArrayList<>();
        java.util.Map<String, String> descricaoItens = new HashMap<>();

        try (InputStream is = getClass().getResourceAsStream("/com/Vixus/inc/resources/itens.txt");
        	     BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
        	    if (is == null) {
        	        JOptionPane.showMessageDialog(null, "Arquivo itens.txt não encontrado no classpath!");
        	        return;
        	    }
        	    String linha;
        	    while ((linha = br.readLine()) != null) {
        	        if (linha.trim().isEmpty() || linha.startsWith("#")) continue;
        	        String[] partes = linha.split(";");
        	        if (partes.length < 2) continue;
        	        String nome = partes[0].trim();
        	        String desc = partes[1].trim();
        	        itensDisponiveis.add(nome);
        	        descricaoItens.put(nome, desc);
        	    }
        	} catch (IOException e) {
        	    JOptionPane.showMessageDialog(null, "Erro ao carregar itens disponíveis: " + e.getMessage());
        	}

        // ComboBox dos itens para adicionar
        JComboBox<String> comboItens = new JComboBox<>(itensDisponiveis.toArray(new String[0]));

        // Botão para adicionar o item selecionado ao inventário
        JButton btnAdicionar = new JButton("Adicionar Item");

        // Painel inferior para o botão e combobox
        JPanel painelAdicionar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        painelAdicionar.add(comboItens);
        painelAdicionar.add(btnAdicionar);

        jogadorCombo.addActionListener(e -> {
            painelItens.removeAll();
            Jogador jogador = (Jogador) jogadorCombo.getSelectedItem();
            if (jogador == null) return;
            String caminho = "src/com/Vixus/inc/resources/inventario_" + jogador.getNome() + ".txt";

            // Mapa para contar itens e quantidades
            Map<String, Integer> mapaItens = new LinkedHashMap<>();
            Map<String, String> mapaDescricoes = new HashMap<>();

            try (BufferedReader br = new BufferedReader(new FileReader(caminho))) {
                String linha;
                while ((linha = br.readLine()) != null) {
                    String[] partes = linha.split(";");
                    if (partes.length < 3) continue;

                    String nomeItem = partes[0].trim();
                    String descricao = partes[1].trim();
                    int qtd = Integer.parseInt(partes[2].trim());

                    mapaItens.put(nomeItem, mapaItens.getOrDefault(nomeItem, 0) + qtd);
                    mapaDescricoes.put(nomeItem, descricao);
                }
            } catch (IOException ex) {
                // Pode ser que o arquivo não exista ainda, sem problema
            }

            // Exibir itens em lista vertical com quantidade
            for (Map.Entry<String, Integer> entry : mapaItens.entrySet()) {
                String nomeItem = entry.getKey();
                int qtd = entry.getValue();
                String descricao = mapaDescricoes.getOrDefault(nomeItem, "");

                JButton itemBtn = new JButton(nomeItem + " x" + qtd);
                itemBtn.setToolTipText(descricao);

                itemBtn.addActionListener(evt -> {
                    usarItem(nomeItem, jogador);
                    removerItemDoArquivo(caminho, nomeItem);
                    // Atualizar inventário após uso
                    jogadorCombo.getActionListeners()[0].actionPerformed(null);
                });

                painelItens.add(itemBtn);
            }

            painelItens.revalidate();
            painelItens.repaint();
        });

        // Função do botão Adicionar Item
        btnAdicionar.addActionListener(e -> {
            Jogador jogador = (Jogador) jogadorCombo.getSelectedItem();
            if (jogador == null) return;

            String nomeItem = (String) comboItens.getSelectedItem();
            if (nomeItem == null) return;

            String descricao = descricaoItens.getOrDefault(nomeItem, "Sem descrição");

            String caminho = "src/com/Vixus/inc/resources/inventario_" + jogador.getNome() + ".txt";

            // Lê inventário atual
            Map<String, String[]> mapaInventario = new LinkedHashMap<>();
            try (BufferedReader br = new BufferedReader(new FileReader(caminho))) {
                String linha;
                while ((linha = br.readLine()) != null) {
                    String[] partes = linha.split(";");
                    if (partes.length < 3) continue;
                    mapaInventario.put(partes[0].trim(), new String[]{partes[1].trim(), partes[2].trim()});
                }
            } catch (IOException ex) {
                // Arquivo pode não existir, tudo bem
            }

            // Adiciona ou incrementa item
            if (mapaInventario.containsKey(nomeItem)) {
                int qtdAtual = Integer.parseInt(mapaInventario.get(nomeItem)[1]);
                qtdAtual++;
                mapaInventario.put(nomeItem, new String[]{descricao, Integer.toString(qtdAtual)});
            } else {
                mapaInventario.put(nomeItem, new String[]{descricao, "1"});
            }

            // Salva inventário atualizado no arquivo
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(caminho))) {
                for (Map.Entry<String, String[]> entry : mapaInventario.entrySet()) {
                    String linha = entry.getKey() + ";" + entry.getValue()[0] + ";" + entry.getValue()[1];
                    bw.write(linha);
                    bw.newLine();
                }
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(dialog, "Erro ao salvar inventário: " + ex.getMessage());
                return;
            }

            // Atualizar lista de itens exibida
            jogadorCombo.getActionListeners()[0].actionPerformed(null);
        });

        jogadorCombo.setSelectedIndex(0);

        dialog.add(jogadorCombo, BorderLayout.NORTH);
        dialog.add(new JScrollPane(painelItens), BorderLayout.CENTER);
        dialog.add(painelAdicionar, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }
    
    static class ItemInventario {
        String nome;
        String descricao;
        int quantidade;

        public ItemInventario(String nome, String descricao, int quantidade) {
            this.nome = nome;
            this.descricao = descricao;
            this.quantidade = quantidade;
        }

        @Override
        public String toString() {
            return nome + " x" + quantidade;
        }
        
        
    }
    
    private void removerItemDoArquivo(String caminho, String nomeItem) {
        try {
        	java.util.List<String> linhas = Files.readAllLines(Paths.get(caminho));
        	java.util.List<String> novas = new ArrayList<>();

            for (String linha : linhas) {
                String[] partes = linha.split(";");
                if (partes.length < 3) {
                    novas.add(linha);
                    continue;
                }

                String nome = partes[0].trim();
                String desc = partes[1].trim();
                int qtd = Integer.parseInt(partes[2].trim());

                if (nome.equals(nomeItem)) {
                    if (qtd > 1) {
                        novas.add(nome + ";" + desc + ";" + (qtd - 1));
                    }
                    // Remove se for só 1
                } else {
                    novas.add(linha);
                }
            }

            Files.write(Paths.get(caminho), novas);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Erro ao atualizar item: " + e.getMessage());
        }
    }
    
    private static final Map<String, String[]> efeitosItens = new HashMap<>() {{
    	put("Descartar Energia", new String[]{"Energia/Bateria", "0.25"});      // Recupera 25% de bateria
    	put("Forçar Resfriamento", new String[]{"Energia/Bateria", "0.50"});      // Recupera 50% de bateria
        put("Cápsula de HP Pequena", new String[]{"HP", "0.10"});      // Recupera 10% do HP máximo
        put("Cápsula de HP Média", new String[]{"HP", "0.25"});      // Recupera 25% do HP máximo
        put("Cápsula de HP Grande", new String[]{"HP", "0.50"});      // Recupera 50% do HP máximo
        put("Cápsula de HP Suprema", new String[]{"HP", "1.0"});      // Recupera 100% do HP máximo
        put("Cápsula de energia Pequena", new String[]{"Energia/Bateria", "0.10"});      // Recupera 10% da Energia máxima
        put("Cápsula de energia Média", new String[]{"Energia/Bateria", "0.25"});      // Recupera 25% da Energia máxima
        put("Cápsula de energia Grande", new String[]{"Energia/Bateria", "0.50"});      // Recupera 50% da Energia máxima
        put("Cápsula de energia Suprema", new String[]{"Energia/Bateria", "1.0"});      // Recupera 100% da Energia máxima
        put("Cápsula de forma Pequena", new String[]{"Pontos de Forma", "10"});  // Recupera 25 pontos fixos
        put("Cápsula de forma Média", new String[]{"Pontos de Forma", "25"});  // Recupera 50 pontos fixos
        put("Cápsula de forma Grande", new String[]{"Pontos de Forma", "50"}); // Recupera 100 pontos fixos
        put("Cápsula de forma Suprema", new String[]{"Pontos de Forma", "100"}); // Recupera 250 pontos fixos
        put("Cápsula Sonora Pequena", new String[]{"HP", "0.20"});      // Recupera 10% do HP máximo
        put("Cápsula Sonora Média", new String[]{"HP", "0.40"});      // Recupera 25% do HP máximo
        put("Cápsula Sonora Grande", new String[]{"HP", "0.60"});      // Recupera 50% do HP máximo
        put("Cápsula MIX Pequena", new String[]{"HP & Energia", "0.10"});      // Recupera 10% do HP e energia máxima
        put("Cápsula MIX Média", new String[]{"HP & Energia", "0.25"});      // Recupera 25% do HP e energia máxima
        put("Cápsula MIX Grande", new String[]{"HP & Energia", "0.50"});      // Recupera 50% do HP e energia máxima
        put("Semente dos Deuses", new String[]{"Restauração Total", "1.0"}); // restaura todos os atributos em 100% 
    }};

    private void usarItem(String nomeItem, Jogador jogador) {
        if (!efeitosItens.containsKey(nomeItem)) {
            JOptionPane.showMessageDialog(this, "Este item não pode ser usado.");
            return;
        }
        String[] efeito = efeitosItens.get(nomeItem);
        String tipo = efeito[0];
        
        if (jogador.isAndroide() && (tipo.equals("Energia") || tipo.equals("HP & Energia"))) {
            JOptionPane.showMessageDialog(this, "Androides não usam este tipo de item!");
            return;
        }
        
        double percentual = Double.parseDouble(efeito[1]);

        switch (tipo) {
            case "HP": {
                BigInteger hpMax = jogador.getHpBase();
                BigInteger cura = new java.math.BigDecimal(hpMax).multiply(new java.math.BigDecimal(percentual)).toBigInteger();
                BigInteger novoHp = jogador.getHP().add(cura);
                if (novoHp.compareTo(hpMax) > 0) novoHp = hpMax;
                jogador.setHp(novoHp);
                JOptionPane.showMessageDialog(this, jogador.getNome() + " recuperou " + formatarNumero(cura) + " de HP!");
                break;
            }
            case "Energia/Bateria": {
                if (jogador.isAndroide()) {
                    // Lógica para recuperar BATERIA
                    double bateriaRecuperada = 100.0 * percentual;
                    double novaBateria = jogador.getBateria() + bateriaRecuperada;
                    novaBateria = Math.min(novaBateria, 100.0); // Limita a 100%
                    
                    if (novaBateria > 0) { // Se a bateria ficou positiva
                        jogador.setInUltradrive(false); // Desativa o modo ULTRADRIVE
                    }
                    
                    jogador.setBateria(novaBateria);
                    JOptionPane.showMessageDialog(this, jogador.getNome() + " recuperou " + String.format("%.1f%%", bateriaRecuperada) + " de bateria!");
                } else {
                    // Lógica para recuperar ENERGIA (para não-androides)
                    BigInteger energiaMax = jogador.getEnergiaBase();
                    BigInteger energiaRecuperada = new BigDecimal(energiaMax).multiply(new BigDecimal(percentual)).toBigInteger();
                    BigInteger novaEnergia = jogador.getEnergia().add(energiaRecuperada);
                    if (novaEnergia.compareTo(energiaMax) > 0) novaEnergia = energiaMax;
                    jogador.setEnergia(novaEnergia);
                    JOptionPane.showMessageDialog(this, jogador.getNome() + " recuperou " + formatarNumero(energiaRecuperada) + " de energia!");
                }
                break;
            }
            
            case "Pontos de Forma": {
                // Ignora se os pontos de forma forem ilimitados
                if (jogador.isFormaIlimitada()) {
                    JOptionPane.showMessageDialog(this, "Este personagem possui Pontos de Forma ilimitados!");
                    return; // Retorna para não consumir o item
                }
                
                // NOVA LÓGICA: Lê o valor fixo do mapa de efeitos
                BigInteger pontosRecuperados = new BigInteger(efeito[1]);
                BigInteger pontosMax = jogador.getFormaPontosBase();
                BigInteger novosPontos = jogador.getFormaPontos().add(pontosRecuperados);

                // Garante que não ultrapasse o máximo
                if (novosPontos.compareTo(pontosMax) > 0) {
                    novosPontos = pontosMax;
                }
                
                jogador.setFormaPontos(novosPontos);
                JOptionPane.showMessageDialog(this, jogador.getNome() + " recuperou " + formatarNumero(pontosRecuperados) + " Pontos de Forma!");
                break;
            }
            
            case "HP & Energia": {
                BigInteger hpMax = jogador.getHpBase();
                BigInteger energiaMax = jogador.getEnergiaBase();

                BigInteger cura = new java.math.BigDecimal(hpMax).multiply(new java.math.BigDecimal(percentual)).toBigInteger();
                BigInteger energiaRecuperada = new java.math.BigDecimal(energiaMax).multiply(new java.math.BigDecimal(percentual)).toBigInteger();

                BigInteger novoHp = jogador.getHP().add(cura);
                if (novoHp.compareTo(hpMax) > 0) novoHp = hpMax;
                jogador.setHp(novoHp);

                BigInteger novaEnergia = jogador.getEnergia().add(energiaRecuperada);
                if (novaEnergia.compareTo(energiaMax) > 0) novaEnergia = energiaMax;
                jogador.setEnergia(novaEnergia);

                // Mensagem formatada corrigida
                JOptionPane.showMessageDialog(this, jogador.getNome() + " recuperou tudo!");
                break;
            }
            case "Restauração Total": {

                jogador.setHp(jogador.getHpBase());
                
                if (!jogador.isFormaIlimitada()) {
                    jogador.setFormaPontos(jogador.getFormaPontosBase());
                }

                if (jogador.isAndroide()) {
                    jogador.setBateria(100.0);
                    jogador.setEnergia(jogador.getEnergiaBase());
                }

                JOptionPane.showMessageDialog(this, jogador.getNome() + " comeu uma Semente dos Deuses e recuperou todas as suas forças!");
                break;
            }
            default:
                JOptionPane.showMessageDialog(this, "Tipo de efeito desconhecido para o item.");
                break;
        }
        atualizarTabela(); 
    }

    
    
    private void abrirPopupFusoes() {
        JDialog popup = new JDialog(this, "Fusões", true);
        popup.setLayout(new BorderLayout());

        JPanel painelForm = new JPanel(new GridLayout(5, 2));

        JComboBox<String> cbJogador1 = new JComboBox<>();
        JComboBox<String> cbJogador2 = new JComboBox<>();
        JComboBox<String> cbComandante = new JComboBox<>();
        JComboBox<String> cbTipoFusao = new JComboBox<>(new String[]{"Majin", "Metamaru", "Potara"});
        JTextField txtNomeFusao = new JTextField();

        for (Jogador j : listaJogadores) {
            if (!j.getNome().contains("(fundido)")) {
                cbJogador1.addItem(j.getNome());
                cbJogador2.addItem(j.getNome());
            }
        }

        cbJogador1.addActionListener(e -> atualizarComandanteCombo(cbComandante, cbJogador1, cbJogador2));
        cbJogador2.addActionListener(e -> atualizarComandanteCombo(cbComandante, cbJogador1, cbJogador2));

        atualizarComandanteCombo(cbComandante, cbJogador1, cbJogador2);

        painelForm.add(new JLabel("Jogador 1:"));
        painelForm.add(cbJogador1);
        painelForm.add(new JLabel("Jogador 2:"));
        painelForm.add(cbJogador2);
        painelForm.add(new JLabel("Comandante:"));
        painelForm.add(cbComandante);
        painelForm.add(new JLabel("Nome da Fusão:"));
        painelForm.add(txtNomeFusao);
        painelForm.add(new JLabel("Tipo de Fusão:"));
        painelForm.add(cbTipoFusao);

        // Lista de fusões existentes
        DefaultListModel<String> modeloFusoes = new DefaultListModel<>();
        JList<String> listaFusoes = new JList<>(modeloFusoes);
        for (String nomeFusao : fusoesAtivas.keySet()) {
            modeloFusoes.addElement(nomeFusao);
        }

        JPanel painelAcoes = new JPanel();
        JButton btnFundir = new JButton("Fundir");
        JButton btnDesfundir = new JButton("Desfundir Selecionado");
        JButton btnFechar = new JButton("Fechar");

        btnFundir.addActionListener(e -> {
            String nome1 = (String) cbJogador1.getSelectedItem();
            String nome2 = (String) cbJogador2.getSelectedItem();
            String comandanteNome = (String) cbComandante.getSelectedItem();
            String tipo = (String) cbTipoFusao.getSelectedItem();
            String nomeFusao = txtNomeFusao.getText().trim();

            if (nome1.equals(nome2)) {
                JOptionPane.showMessageDialog(popup, "Escolha dois jogadores diferentes.");
                return;
            }
            if (nomeFusao.isEmpty()) {
                JOptionPane.showMessageDialog(popup, "Digite um nome para a fusão.");
                return;
            }

            Jogador j1 = buscarJogadorPorNome(nome1);
            Jogador j2 = buscarJogadorPorNome(nome2);
            Jogador comandante = buscarJogadorPorNome(comandanteNome);

            if (j1 != null && j2 != null && comandante != null) {
                realizarFusao(j1, j2, comandante, tipo, nomeFusao);
                modeloFusoes.addElement(nomeFusao);
            }
        });

        btnDesfundir.addActionListener(e -> {
            String fusaoSelecionada = listaFusoes.getSelectedValue();
            if (fusaoSelecionada != null) {
                desfazerFusao(fusaoSelecionada);
                modeloFusoes.removeElement(fusaoSelecionada);
            }
        });

        btnFechar.addActionListener(e -> popup.dispose());

        painelAcoes.add(btnFundir);
        painelAcoes.add(btnDesfundir);
        painelAcoes.add(btnFechar);

        popup.add(painelForm, BorderLayout.NORTH);
        popup.add(new JScrollPane(listaFusoes), BorderLayout.CENTER);
        popup.add(painelAcoes, BorderLayout.SOUTH);

        popup.setSize(500, 400);
        popup.setLocationRelativeTo(this);
        popup.setVisible(true);
    }

    private void atualizarComandanteCombo(JComboBox<String> cbComandante, JComboBox<String> cb1, JComboBox<String> cb2) {
        cbComandante.removeAllItems();
        String nome1 = (String) cb1.getSelectedItem();
        String nome2 = (String) cb2.getSelectedItem();
        if (nome1 != null && nome2 != null && !nome1.equals(nome2)) {
            cbComandante.addItem(nome1);
            cbComandante.addItem(nome2);
            }
        }
    
    private final Map<String, Jogador[]> fusoesAtivas = new HashMap<>();

    private void realizarFusao(Jogador j1, Jogador j2, Jogador comandante, String tipo, String nomeFusao) {
        Jogador outro = (comandante == j1) ? j2 : j1;
        BigInteger energia, hp, atk, velocidade, pontosdeforma;

        switch (tipo.toLowerCase()) {
            case "majin":
                hp = j1.getHP().add(j2.getHP());
                atk = j1.getAtk().add(j2.getAtk());
                energia = j1.getEnergia();
                velocidade = j1.getVelocidade().add(j2.getVelocidade());
                pontosdeforma = j1.getFormaPontos().add(j2.getFormaPontos()).divide(BigInteger.valueOf(2));
                break;
            case "metamaru":
                hp = j1.getHP().add(j2.getHP()).multiply(BigInteger.valueOf(2));
                atk = j1.getAtk().add(j2.getAtk()).multiply(BigInteger.valueOf(2));
                energia = j1.getEnergia().add(j2.getEnergia());
                velocidade = j1.getVelocidade().add(j2.getVelocidade()).multiply(BigInteger.valueOf(2));
                pontosdeforma = j1.getFormaPontos().add(j2.getFormaPontos());
                break;
            case "potara":
                hp = j1.getHP().multiply(j2.getHP());
                atk = j1.getAtk().multiply(j2.getAtk());
                energia = (j1.getEnergia().add(j2.getEnergia())).multiply(BigInteger.valueOf(2));
                velocidade = j1.getVelocidade().multiply(j2.getVelocidade());
                pontosdeforma = j1.getFormaPontos().add(j2.getFormaPontos());
                break;
            default:
                JOptionPane.showMessageDialog(this, "Tipo de fusão inválido.");
                return;
        }

        Jogador fusao = new Jogador(nomeFusao, hp, atk, energia, velocidade, pontosdeforma,
                                    j1.getDadoP(), j1.getDadoC(), j1.getDadoI(), j1.getDadoA(), j1.getDadoL());
        
        int indexComandante = listaJogadores.indexOf(comandante);
        listaJogadores.set(indexComandante, fusao);
        listaJogadores.remove(outro);
        fusoesAtivas.put(nomeFusao, new Jogador[]{comandante, outro});
        atualizarTabela();
    }
    
    private void desfazerFusao(String nomeFusao) {
        Jogador[] jogadoresOriginais = fusoesAtivas.get(nomeFusao);
        if (jogadoresOriginais != null) {
            Jogador comandante = jogadoresOriginais[0];
            Jogador outro = jogadoresOriginais[1];

            // Remove a fusão da lista
            listaJogadores.removeIf(j -> j.getNome().equals(nomeFusao));

            // Restaura os dois jogadores originais
            listaJogadores.add(comandante);
            listaJogadores.add(outro);

            fusoesAtivas.remove(nomeFusao);
            atualizarTabela();
        }
    }

    public Jogador buscarJogadorPorNome(String nome) {
        for (Jogador j : listaJogadores) {
            if (j.getNome().equals(nome)) return j;
        }
        return null;
    }

    private void aplicarTransformacaoOuAmpliacao(String tipo, Object alvo, String formaNome) {
        if (alvo == null) return;

        if (tipo.equals("Transformação")) {
            Transformacao t = listaTransformacoes.stream().filter(tr -> tr.getNome().equals(formaNome)).findFirst().orElse(null);
            if (t == null) return;
            
            if (alvo instanceof Jogador j) {
                j.transformar(t);
                j.reduzirFormaPontos(t.getCustoPorTurno());
                j.formaPontosAtual = j.getFormaPontos().add(BigInteger.valueOf(t.getCustoPorTurno()));
            } else if (alvo instanceof Inimigo i) {
                i.transformar(t);
                i.reduzirFormaPontos(t.getCustoPorTurno());
                i.formaPontosAtual = i.getFormaPontos().add(BigInteger.valueOf(t.getCustoPorTurno()));
            }

        } else if (tipo.equals("Ampliação")) {
            Ampliacao a = listaAmpliacoes.stream().filter(am -> am.getNome().equals(formaNome)).findFirst().orElse(null);
            if (a == null) return;
            
            if (alvo instanceof Jogador j) {
                j.ampliar(a);
                j.reduzirFormaPontos(a.getCustoPorTurno());
                j.formaPontosAtual = j.getFormaPontos().add(BigInteger.valueOf(a.getCustoPorTurno()));
            } else if (alvo instanceof Inimigo i) {
                i.ampliar(a);
                i.reduzirFormaPontos(a.getCustoPorTurno());
                i.formaPontosAtual = i.getFormaPontos().add(BigInteger.valueOf(a.getCustoPorTurno()));
            }
        }
    }

    private void abrirPopupGerenciarInimigos() {
        JDialog popup = new JDialog(this, "Gerenciar Inimigos", true);
        popup.setLayout(new BorderLayout());
        DefaultListModel<String> modeloLista = new DefaultListModel<>();
        for (Inimigo i : listaInimigos) {
            modeloLista.addElement(i.getNome());
        }
        JList<String> lista = new JList<>(modeloLista);
        popup.add(new JScrollPane(lista), BorderLayout.CENTER);

        JPanel painelBotoes = new JPanel();
        JButton btnAdicionar = new JButton("Adicionar");

        // --- LÓGICA DO BOTÃO ATUALIZADA ---
        btnAdicionar.addActionListener(e -> {
            // 1. Pede todas as informações em um único input.
            String dadosCompletos = JOptionPane.showInputDialog(
                popup,
                "Insira os dados do inimigo\nformato:Nome;HP;ATK;Velocidade;Energia;PontosForma",
                ""
            );

            // 2. Verifica se o usuário não cancelou ou inseriu um texto vazio.
            if (dadosCompletos != null && !dadosCompletos.trim().isEmpty()) {
                try {
                    // 3. Reutiliza o método fromLinha para criar o inimigo!
                    Inimigo novoInimigo = Inimigo.fromLinha(dadosCompletos);
                    
                    if (novoInimigo != null) {
                        // 4. Adiciona o novo inimigo à lista e atualiza a interface.
                        listaInimigos.add(novoInimigo);
                        modeloLista.addElement(novoInimigo.getNome());
                        atualizarTabela();
                    } else {
                        // fromLinha retorna null se o formato estiver incorreto (ex: poucas partes)
                        JOptionPane.showMessageDialog(popup, "Formato inválido. Use 6 campos separados por ponto e vírgula ';'.", "Erro de Formato", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception ex) {
                    // Captura erros de conversão de número (se o usuário digitar texto onde deveria ser número)
                    JOptionPane.showMessageDialog(popup, "Erro nos valores numéricos. Verifique os dados e tente novamente.", "Erro de Formato", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        JButton btnRemover = new JButton("Remover Selecionado");
        btnRemover.addActionListener(e -> {
            int idx = lista.getSelectedIndex();
            if (idx >= 0) {
                listaInimigos.remove(idx);
                modeloLista.remove(idx);
                atualizarTabela();
            }
        });

        JButton btnFechar = new JButton("Fechar");
        btnFechar.addActionListener(e -> popup.dispose());

        painelBotoes.add(btnAdicionar);
        painelBotoes.add(btnRemover);
        painelBotoes.add(btnFechar);
        popup.add(painelBotoes, BorderLayout.SOUTH);
        popup.setSize(400, 300);
        popup.setLocationRelativeTo(this);
        popup.setVisible(true);
    }
    
    public static void main(String[] args) {

    	SwingUtilities.invokeLater(() -> {

    	RPGManager app = new RPGManager();

    	app.setVisible(true);

    	});

    	}

    // --- Classes auxiliares ---

    static class Jogador {
        private String nome;
        private BigInteger atkBase, hpBase, energiaBase, velocidadeBase, formaPontosBase;
        private BigInteger atkAtual, hpAtual, energiaAtual, velocidadeAtual, formaPontosAtual;
        
        private String dadoP, dadoC, dadoI, dadoA, dadoL;

        private Transformacao transformacaoAtual;
        private Ampliacao ampliacaoAtual;
        
        private final boolean isAndroide;
        private final boolean formaIlimitada;
        
        private boolean isInUltradrive = false;
        
        private static double PONTOS_ENERGIA_POR_BATERIA = 4.0;
        
        private double bateria;

        public Jogador(String nome, BigInteger hp, BigInteger atk,  BigInteger energia, BigInteger velocidade, BigInteger formaPontos,
                       String dadoP, String dadoC, String dadoI, String dadoA, String dadoL) {
            this.nome = nome;
            this.atkBase = atk;
            this.hpBase = hp;
            this.energiaBase = energia;
            this.velocidadeBase = velocidade;
            this.formaPontosBase = formaPontos;
            //this.formaPontos = formaPontos;
            this.atkAtual = new BigInteger(atk.toByteArray());
            this.hpAtual = new BigInteger(hp.toByteArray());
            this.energiaAtual = new BigInteger(energia.toByteArray());
            this.velocidadeAtual = new BigInteger(velocidade.toByteArray());
            this.formaPontosAtual = new  BigInteger(formaPontos.toByteArray());
            
            this.isAndroide = energia.equals(BigInteger.valueOf(-1));
            this.formaIlimitada = formaPontos.equals(BigInteger.valueOf(-1));
            
            if (this.isAndroide) {
                this.bateria = 100.0;
                this.energiaAtual = BigInteger.ZERO; // Energia normal não é usada
            } else {
                this.bateria = 0.0;
                this.energiaAtual = new BigInteger(energia.toByteArray());
            }
            
            this.dadoP = dadoP;
            this.dadoC = dadoC;
            this.dadoI = dadoI;
            this.dadoA = dadoA;
            this.dadoL = dadoL;
        }

        public void atualizarAtributos() {
            double multiplicadorTotal = 1.0;
            if (transformacaoAtual != null) multiplicadorTotal *= transformacaoAtual.getMultiplicador();
            if (ampliacaoAtual != null) multiplicadorTotal *= ampliacaoAtual.getMultiplicador();
            
            // ATENÇÃO: Agora só atualiza os atributos derivados. HP e Energia não são mais tocados aqui!
            atkAtual = new java.math.BigDecimal(atkBase).multiply(new java.math.BigDecimal(multiplicadorTotal)).toBigInteger();
            velocidadeAtual = new java.math.BigDecimal(velocidadeBase).multiply(new java.math.BigDecimal(multiplicadorTotal)).toBigInteger();
        }
        
        public boolean isAndroide() { return isAndroide; }
        public double getBateria() { return bateria; }
        public void setBateria(double bateria) { this.bateria = bateria; }
        public boolean isInUltradrive() { return isInUltradrive; }
        public void setInUltradrive(boolean status) { this.isInUltradrive = status; }
        public double getEnergiaEquivalente() { return this.isAndroide ? this.bateria * PONTOS_ENERGIA_POR_BATERIA : 0; }



        public static Jogador fromLinha(String linha) {
            String[] p = linha.split(";");
            if (p.length < 11) {
                System.err.println("Linha de jogador mal formatada: " + linha);
                return null;
            }
            return new Jogador(
                    p[0],
                    new BigInteger(p[1]), new BigInteger(p[2]), new BigInteger(p[3]),
                    new BigInteger(p[4]), new BigInteger(p[5]),
                    p[6], p[7], p[8], p[9], p[10]
            );
        }


        // Getters e setters

     // Getters e Setters
        public boolean isFormaIlimitada() { return formaIlimitada; }
        public String getNome() { return nome; }
        public String toString() { return nome; }
        public BigInteger getAtk() {
            if (isAndroide()) {
                if (isInUltradrive()) {
                    return atkAtual.multiply(BigInteger.valueOf(3)); // 3x para ULTRADrive
                } else if (getBateria() <= 0) {
                    return atkAtual.multiply(BigInteger.valueOf(2)); // 2x para Overdrive
                }
            }
            return atkAtual; // Ataque normal
        }
        public BigInteger getHP() { return hpAtual; }
        public BigInteger getEnergia() { return energiaAtual; }
        public BigInteger getVelocidade() { return velocidadeAtual; }
        public BigInteger getFormaPontos() { return formaPontosAtual; }
        public Transformacao getTransformacao() { return transformacaoAtual; }
        public Ampliacao getAmpliacao() { return ampliacaoAtual; }
        public String getDadoP() { return dadoP; }
        public String getDadoC() { return dadoC; }
        public String getDadoI() { return dadoI; }
        public String getDadoA() { return dadoA; }
        public String getDadoL() { return dadoL; }
        public BigInteger getHpBase() { return hpBase; }
        public BigInteger getEnergiaBase() { return energiaBase; }
        public BigInteger getFormaPontosBase() { return formaPontosBase; }
        
        public void setHp(BigInteger hp) { this.hpAtual = hp; }
        public void setAtk(BigInteger atk) { this.atkAtual = atk; }
        public void setVelocidade(BigInteger velocidade) { this.velocidadeAtual = velocidade; }
        public void setEnergia(BigInteger energia) { this.energiaAtual = energia; }
        public void setFormaPontos(BigInteger pontosForma) { this.formaPontosAtual = pontosForma; }
        public void reduzirFormaPontos(long l) { 
        	this.formaPontosAtual = this.formaPontosAtual.subtract(BigInteger.valueOf(l)); 
        	if (this.formaPontosAtual.compareTo(BigInteger.ZERO) < 0) 
        		this.formaPontosAtual = BigInteger.ZERO; 
        	}
        public void transformar(Transformacao t) {
            // Calcula a porcentagem de vida ATUAL antes de transformar
            BigDecimal hpMaxAnterior = new java.math.BigDecimal(this.hpBase).multiply(new java.math.BigDecimal(getMultiplicadorAtual()));
            BigDecimal percentualVida = new java.math.BigDecimal(this.hpAtual).divide(hpMaxAnterior, 10, java.math.RoundingMode.HALF_UP);
            
            this.transformacaoAtual = t;
            atualizarAtributos(); // Atualiza ATK e Velocidade

            // Aplica o novo multiplicador ao HP máximo e ajusta o HP atual para a mesma porcentagem
            BigDecimal hpMaxNovo = new java.math.BigDecimal(this.hpBase).multiply(new java.math.BigDecimal(getMultiplicadorAtual()));
            this.hpAtual = hpMaxNovo.multiply(percentualVida).toBigInteger();
        }
        
        public void ampliar(Ampliacao a) {
            BigDecimal hpMaxAnterior = new java.math.BigDecimal(this.hpBase).multiply(new java.math.BigDecimal(getMultiplicadorAtual()));
            BigDecimal percentualVida = new java.math.BigDecimal(this.hpAtual).divide(hpMaxAnterior, 10, java.math.RoundingMode.HALF_UP);
            
            this.ampliacaoAtual = a;
            atualizarAtributos();

            BigDecimal hpMaxNovo = new java.math.BigDecimal(this.hpBase).multiply(new java.math.BigDecimal(getMultiplicadorAtual()));
            this.hpAtual = hpMaxNovo.multiply(percentualVida).toBigInteger();
        }
        
        public void destransformar() {
            if (this.transformacaoAtual != null) {
                // Mantém a porcentagem de vida atual
                BigDecimal hpMaxAnterior = new BigDecimal(this.hpBase).multiply(new BigDecimal(getMultiplicadorAtual()));
                BigDecimal percentualVida = new BigDecimal(this.hpAtual).divide(hpMaxAnterior, 10, java.math.RoundingMode.HALF_UP);

                this.transformacaoAtual = null;
                atualizarAtributos(); // Recalcula ATK e Velocidade a partir do BASE

                // Reajusta o HP para a nova base
                BigDecimal hpMaxNovo = new BigDecimal(this.hpBase).multiply(new BigDecimal(getMultiplicadorAtual()));
                this.hpAtual = hpMaxNovo.multiply(percentualVida).toBigInteger();
            }
        }

        public void desampliar() {
            if (this.ampliacaoAtual != null) {
                BigDecimal hpMaxAnterior = new BigDecimal(this.hpBase).multiply(new BigDecimal(getMultiplicadorAtual()));
                BigDecimal percentualVida = new BigDecimal(this.hpAtual).divide(hpMaxAnterior, 10, java.math.RoundingMode.HALF_UP);

                this.ampliacaoAtual = null;
                atualizarAtributos();

                BigDecimal hpMaxNovo = new BigDecimal(this.hpBase).multiply(new BigDecimal(getMultiplicadorAtual()));
                this.hpAtual = hpMaxNovo.multiply(percentualVida).toBigInteger();
            }
        }
        
        private double getMultiplicadorAtual() {
            double m = 1.0;
            if (transformacaoAtual != null) m *= transformacaoAtual.getMultiplicador();
            if (ampliacaoAtual != null) m *= ampliacaoAtual.getMultiplicador();
            return m;
        }
        
        public void setMultiplicador(Transformacao t) { 
        	this.transformacaoAtual = t; }
    }


    static class Inimigo {
        private String nome;
        // Atributos Base (não mudam)
        private BigInteger atkBase, hpBase, energiaBase, velocidadeBase, formaPontosBase;
        
        // Atributos Atuais (afetados por transformações)
        private BigInteger atkAtual, hpAtual, energiaAtual, velocidadeAtual, formaPontosAtual;

        private Transformacao transformacaoAtual;
        private Ampliacao ampliacaoAtual;

        public Inimigo(String nome, BigInteger hp, BigInteger atk, BigInteger velocidade, BigInteger energia, BigInteger formaPontos) {
            this.nome = nome;
            this.hpBase = hp;
            this.atkBase = atk;
            this.velocidadeBase = velocidade;
            this.energiaBase = energia;
            this.formaPontosBase = formaPontos;

            // Atributos atuais começam iguais aos base
            this.hpAtual = new BigInteger(hp.toByteArray());
            this.atkAtual = new BigInteger(atk.toByteArray());
            this.velocidadeAtual = new BigInteger(velocidade.toByteArray());
            this.energiaAtual = new BigInteger(energia.toByteArray());
            this.formaPontosAtual = new BigInteger(formaPontos.toByteArray());
        }
        
        // Método para criar Inimigo a partir de uma linha de texto
        public static Inimigo fromLinha(String linha) {
            String[] p = linha.split(";");
            if (p.length < 6) {
                System.err.println("Linha de inimigo mal formatada: " + linha);
                return null;
            }
            return new Inimigo(
                    p[0],
                    new BigInteger(p[1]), // HP
                    new BigInteger(p[2]), // ATK
                    new BigInteger(p[3]), // Velocidade
                    new BigInteger(p[4]), // Energia
                    new BigInteger(p[5])  // Pontos de Forma
            );
        }

        // Método para recalcular atributos (copiado do Jogador)
        public void atualizarAtributos() {
            double multiplicadorTotal = getMultiplicadorAtual();
            
            // CORREÇÃO: Usar os atributos BASE para o cálculo
            atkAtual = new java.math.BigDecimal(atkBase).multiply(new java.math.BigDecimal(multiplicadorTotal)).toBigInteger();
            hpAtual = new java.math.BigDecimal(hpBase).multiply(new java.math.BigDecimal(multiplicadorTotal)).toBigInteger();
            velocidadeAtual = new java.math.BigDecimal(velocidadeBase).multiply(new java.math.BigDecimal(multiplicadorTotal)).toBigInteger();
        }

        // Métodos de transformação (copiados do Jogador)
        public void transformar(Transformacao t) {
            // Calcula a porcentagem de vida ATUAL antes de transformar
            BigDecimal hpMaxAnterior = new java.math.BigDecimal(this.hpBase).multiply(new java.math.BigDecimal(getMultiplicadorAtual()));
            BigDecimal percentualVida = new java.math.BigDecimal(this.hpAtual).divide(hpMaxAnterior, 10, java.math.RoundingMode.HALF_UP);
            
            this.transformacaoAtual = t;
            atualizarAtributos(); // Atualiza ATK e Velocidade

            // Aplica o novo multiplicador ao HP máximo e ajusta o HP atual para a mesma porcentagem
            BigDecimal hpMaxNovo = new java.math.BigDecimal(this.hpBase).multiply(new java.math.BigDecimal(getMultiplicadorAtual()));
            this.hpAtual = hpMaxNovo.multiply(percentualVida).toBigInteger();
        }
        
        public void ampliar(Ampliacao a) { 
        	BigDecimal hpMaxAnterior = new java.math.BigDecimal(this.hpBase).multiply(new java.math.BigDecimal(getMultiplicadorAtual()));
            BigDecimal percentualVida = new java.math.BigDecimal(this.hpAtual).divide(hpMaxAnterior, 10, java.math.RoundingMode.HALF_UP);
            
            this.ampliacaoAtual = a;
            atualizarAtributos();

            BigDecimal hpMaxNovo = new java.math.BigDecimal(this.hpBase).multiply(new java.math.BigDecimal(getMultiplicadorAtual()));
            this.hpAtual = hpMaxNovo.multiply(percentualVida).toBigInteger();
        	}
        
        public void destransformar() {
            if (this.transformacaoAtual != null) {
                // Mantém a porcentagem de vida atual
                BigDecimal hpMaxAnterior = new BigDecimal(this.hpBase).multiply(new BigDecimal(getMultiplicadorAtual()));
                BigDecimal percentualVida = new BigDecimal(this.hpAtual).divide(hpMaxAnterior, 10, java.math.RoundingMode.HALF_UP);

                this.transformacaoAtual = null;
                atualizarAtributos(); // Recalcula ATK e Velocidade a partir do BASE

                // Reajusta o HP para a nova base
                BigDecimal hpMaxNovo = new BigDecimal(this.hpBase).multiply(new BigDecimal(getMultiplicadorAtual()));
                this.hpAtual = hpMaxNovo.multiply(percentualVida).toBigInteger();
            }
        }

        public void desampliar() {
            if (this.ampliacaoAtual != null) {
                BigDecimal hpMaxAnterior = new BigDecimal(this.hpBase).multiply(new BigDecimal(getMultiplicadorAtual()));
                BigDecimal percentualVida = new BigDecimal(this.hpAtual).divide(hpMaxAnterior, 10, java.math.RoundingMode.HALF_UP);

                this.ampliacaoAtual = null;
                atualizarAtributos();

                BigDecimal hpMaxNovo = new BigDecimal(this.hpBase).multiply(new BigDecimal(getMultiplicadorAtual()));
                this.hpAtual = hpMaxNovo.multiply(percentualVida).toBigInteger();
            }
        }
        
        public void reduzirFormaPontos(long l) { 
        	this.formaPontosAtual = this.formaPontosAtual.subtract(BigInteger.valueOf(l)); 
        	if (this.formaPontosAtual.compareTo(BigInteger.ZERO) < 0) 
        		this.formaPontosAtual = BigInteger.ZERO; 
        	}
        public void setFormaPontos(BigInteger pontosForma) { this.formaPontosAtual = pontosForma; }
        private double getMultiplicadorAtual() {
            double m = 1.0;
            if (transformacaoAtual != null) m *= transformacaoAtual.getMultiplicador();
            if (ampliacaoAtual != null) m *= ampliacaoAtual.getMultiplicador();
            return m;
        }
        // Getters e Setters
        public String getNome() { return nome; }
        public BigInteger getHp() { return hpAtual; }
        public BigInteger getAtk() { return atkAtual; }
        public BigInteger getVelocidade() { return velocidadeAtual; }
        public BigInteger getEnergia() { return energiaAtual; }
        public BigInteger getFormaPontos() { return formaPontosAtual; }
        public Transformacao getTransformacao() { return transformacaoAtual; }
        public Ampliacao getAmpliacao() { return ampliacaoAtual; }
        
        public void setHp(BigInteger hpNovo) { this.hpAtual = hpNovo; }
        public void setEnergia(BigInteger energiaNova) { this.energiaAtual = energiaNova; }
    }


    static class Transformacao {
        private String nome;
        private long multiplicador;
        private long custoPorTurno;

        public Transformacao(String nome, long multiplicador, long custoPorTurno) {
            this.nome = nome;
            this.multiplicador = multiplicador;
            this.custoPorTurno = custoPorTurno;
        }

        public static Transformacao fromLinha(String linha) {
            // Exemplo: SuperSaiyajin;2.0;10
            String[] p = linha.split(";");
            return new Transformacao(p[0], Long.parseLong(p[1]), Long.parseLong(p[2]));
        }

        public String getNome() { return nome; }
        public long getMultiplicador() { return multiplicador; }
        public long getCustoPorTurno() { return custoPorTurno; }
    }

    static class Ampliacao {
        private String nome;
        private int bonusAmpliação;
        private int custoPorTurno;

        public Ampliacao(String nome, int bonusAmpliação, int custoPorTurno) {
            this.nome = nome;
            this.bonusAmpliação = bonusAmpliação;
            this.custoPorTurno = custoPorTurno;
        }

        public static Ampliacao fromLinha(String linha) {
            // Exemplo de linha: Raiva;100;5
            String[] p = linha.split(";");
            return new Ampliacao(
                p[0],
                Integer.parseInt(p[1]),
                Integer.parseInt(p[2])
            );
        }

        public String getNome() {
            return nome;
        }

        public int getBonusAmpliação() {
            return bonusAmpliação;
        }

        public int getCustoPorTurno() {
            return custoPorTurno;
        }

        public int getBonusPorcentagem() {
            return bonusAmpliação; // assumindo que todos os bônus são iguais (100% = dobra o valor)
        }

        public double getMultiplicador() {	
            return 1 + (bonusAmpliação / 100.0);
        }
    }
    
    public static class Tecnica {
        private final String nome;
        private final int custo;
        private final int qtdDados;
        private final int ladosDados;

        public Tecnica(String nome, int custo, int qtdDados, int ladosDados) {
            this.nome = nome;
            this.custo = custo;
            this.qtdDados = qtdDados;
            this.ladosDados = ladosDados;
        }

        public BigInteger rolarDano(BigInteger atkBase) {
            int soma = 0;
            for (int i = 0; i < qtdDados; i++) {
                soma += (int) (Math.random() * ladosDados) + 1;
            }
            // Usa BigDecimal para o cálculo para manter a precisão
            return new java.math.BigDecimal(atkBase)
                    .divide(new java.math.BigDecimal("10.0"))
                    .multiply(new java.math.BigDecimal(soma))
                    .toBigInteger();
        }
        
        public String getNome() { return nome; }
        public int getCusto() { return custo; }
        public String getMultiplicadorTexto() { return qtdDados + "d" + ladosDados; }
        public String toString() { return nome; }
    }


        /*@Override
        public String toString() {
            return nome
        }*/
        
    private void importarTecnicas(String caminho) {
        try (BufferedReader br = new BufferedReader(new FileReader(caminho))) {
            String linha;
            while ((linha = br.readLine()) != null) {
                if (linha.trim().isEmpty() || linha.startsWith("#")) continue;
                String[] partes = linha.split(";");
                if (partes.length != 4) continue;

                String nome = partes[0].trim();
                int custo = Integer.parseInt(partes[1].trim());
                int qtdDados = Integer.parseInt(partes[2].trim());
                int lados = Integer.parseInt(partes[3].trim());

                listaTecnicas.add(new Tecnica(nome, custo, qtdDados, lados));
            }
        } catch (IOException | NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Erro ao carregar técnicas: " + e.getMessage());
        }
    }
    
    class JTableComTooltips extends JTable {

        private RPGManager rpgManager; // Referência para acessar o método de busca

        public JTableComTooltips(DefaultTableModel modelo, RPGManager manager) {
            super(modelo);
            this.rpgManager = manager;
        }

        // Este é o método chave que vamos sobrescrever
        @Override
        public String getToolTipText(MouseEvent event) {
            Point p = event.getPoint();
            int rowIndex = rowAtPoint(p);
            int colIndex = columnAtPoint(p);

            if (rowIndex != -1 && colIndex != -1) {
                String tipo = (String) getModel().getValueAt(rowIndex, 0);
                
                if ("Jogador".equals(tipo)) {
                    String nomeJogador = (String) getModel().getValueAt(rowIndex, 1);
                    Jogador jogador = rpgManager.buscarJogadorPorNome(nomeJogador);

                    if (jogador != null) {
                        // Monta a tooltip usando os dados específicos do jogador
                        return "<html>" +
                               "<b>Testes de Atributo:</b><br>" +
                               "S → " + jogador.getAtk() + "<br>" +
                               "P → " + jogador.getDadoP() + "<br>" +
                               "E → " + jogador.getHP() + "<br>" +
                               "C → " + jogador.getDadoC() + "<br>" +
                               "I → " + jogador.getDadoI() + "<br>" +
                               "A → " + jogador.getDadoA() + "<br>" +
                               "L → " + jogador.getDadoL() +
                               "</html>";
                    }
                }
            }
            return null;
        }
    }
    
    

	public void recarregarDadosEAtualizarTabela() {
		importarDados();
		atualizarTabela();
		JOptionPane.showMessageDialog(this, "Dados do arquivo recarregados e tabela atualizada!");
	}
}
