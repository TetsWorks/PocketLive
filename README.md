# 🎚️ PocketLive

> Uma DAW (Estação de Trabalho de Áudio Digital) mobile para Android — crie beats, sequencie instrumentos e faça remixes direto do seu celular.

![Plataforma](https://img.shields.io/badge/plataforma-Android-brightgreen?style=flat-square&logo=android)
![Linguagem](https://img.shields.io/badge/linguagem-Java-orange?style=flat-square&logo=java)
![SDK Mínimo](https://img.shields.io/badge/minSdk-24%20(Android%207)-blue?style=flat-square)
![Licença](https://img.shields.io/badge/licença-MIT-purple?style=flat-square)
![Status](https://img.shields.io/badge/status-em%20desenvolvimento-yellow?style=flat-square)

---

## ✨ Funcionalidades

- 🥁 **Step Sequencer** — grade de 16 passos por trilha, toque para ativar os beats
- 🎛️ **Sintetizador embutido** — 8 instrumentos gerados matematicamente (sem arquivos de áudio)
  - Kick, Snare, Hi-Hat, Clap, Bass 808, Lead Synth, Pad, Perc
- 🎵 **Importar áudio customizado** — importe qualquer `.mp3`, `.wav` ou `.ogg` do seu celular para remixar
- 🔊 **Controles por trilha** — slider de volume individual e botão de mute
- ⏱️ **Controle de BPM** — ajustável de 40 a 300 BPM
- ▶️ **Transporte** — Play, Pause, Stop
- 🎨 **Multi-trilha** — adicione quantas trilhas quiser, cada uma com sua cor
- 🗑️ **Gerenciamento de trilhas** — limpe os steps ou remova trilhas com um toque longo

---

## 🎹 Instrumentos (Sintetizador)

Todos os sons são gerados em tempo real usando matemática — nenhum arquivo de áudio embutido no app.

| Instrumento | Método de Geração |
|---|---|
| 🥁 Kick | Onda senoidal com decay exponencial de pitch |
| 🪘 Snare | Tom senoidal + burst de ruído branco |
| 🎵 Hi-Hat | Ruído branco filtrado, decay curto |
| 👏 Clap | 3 bursts de ruído sobrepostos |
| 🔈 Bass 808 | Seno com slide de pitch e sustain longo |
| 🎹 Lead Synth | Onda quadrada com envelope ADSR |
| 🌊 Pad | Múltiplos senos detuned |
| 🪃 Perc | Seno curto de pitch alto |

---

## 📦 Estrutura do Projeto

```
app/src/main/java/com/tetsworks/pocketlive/
├── MainActivity.java               # Tela principal, orquestração da UI
├── audio/
│   ├── AudioEngine.java            # Toca sons sintetizados e áudios customizados
│   ├── Sequencer.java              # Motor do loop de steps (clock de BPM)
│   └── Synthesizer.java            # Geração matemática de sons
├── model/
│   └── Track.java                  # Modelo de dados de uma trilha
├── ui/
│   ├── StepButton.java             # View customizada: célula de um step
│   └── TrackRowView.java           # View customizada: linha completa de instrumento
└── viewmodel/
    └── SequencerViewModel.java     # Gerenciamento de estado (LiveData)
```

---

## 🛠️ Como Compilar

### Requisitos
- Android Studio Hedgehog ou mais recente
- Android SDK 36
- Java 17
- Dispositivo mínimo: Android 7.0 (API 24)

### Passos

```bash
# 1. Clone o repositório
git clone https://github.com/seu-usuario/PocketLive.git

# 2. Abra no Android Studio
# Arquivo → Abrir → selecione a pasta PocketLive

# 3. Sincronize o Gradle
# O Android Studio vai solicitar automaticamente

# 4. Rode no dispositivo ou emulador
# Clique em ▶ Executar ou use Shift+F10
```

---

## 🗺️ Roadmap

Funcionalidades planejadas — contribuições são muito bem-vindas!

- [ ] Efeitos por trilha (reverb, delay, distorção)
- [ ] Exportar / gravar sessão em `.wav` ou `.mp3`
- [ ] Múltiplas cenas (como clips do Ableton)
- [ ] Editor de piano roll para trilhas melódicas
- [ ] Controle de pitch por trilha (oitava acima/abaixo)
- [ ] Salvar e carregar padrões
- [ ] Suporte a MIDI
- [ ] Visualização de forma de onda para trilhas de áudio customizado
- [ ] Mais instrumentos sintetizados

---


## 📄 Licença

GPL-3.0 License — veja o arquivo [LICENSE](LICENSE) para detalhes.

---

<p align="center">Feito com ❤️ para quem faz música</p>