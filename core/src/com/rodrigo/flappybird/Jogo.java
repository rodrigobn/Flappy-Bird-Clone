package com.rodrigo.flappybird;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.Random;

public class Jogo extends ApplicationAdapter {

    private SpriteBatch batch; //Renderiza imagens e formas na tela

    //Texturas
    private Texture[] passaros;
    private Texture fundo;
    private Texture canoBaixo;
    private Texture canoTopo;
    private Texture gameOver;

    //Formas para colisão
    private ShapeRenderer shapeRenderer; //desenha as formas nas texturas
    private Circle circuloPassaro;
    private Rectangle rectanguloCanoCima;
    private Rectangle rectanguloCanoBaixo;

    //Atributos de configuração
    private float larguraTela;
    private float alturaTela;
    private float variacao = 0;
    private float gravidade = 0;
    private float delayRender;
    private float possicaoInicialVerticalPassaro = 0;
    private float posicaoCanoHorizontal;
    private float posicaoCanoVertical;
    private float espacoEntreCanos;
    private Random random;
    private int pontos = 0;
    private int velocidadeJogo = 300;
    private int recorde = 0;
    private boolean passouCano = false;
    private int estadoJogo = 0;
    private float posicaoHorizontalPassaro = 0;

    //Exibição de textos
    BitmapFont textoPontuação;
    BitmapFont textoReiniciar;
    BitmapFont textoMelhorPontuação;


    //Configurar sons
    Sound somVoando;
    Sound somColisao;
    Sound somPontuacao;

    //Objeto salvar pontuação
    Preferences preferences;

    //Objetos para camera de visao
    private OrthographicCamera camera;
    private Viewport viewport;
    private final float VIRTUAL_WIDTH = 720;
    private final float VIRTUAL_HEIGTH = 1280;

    @Override
    public void create() {

        inicializarTextura();
        inicializarObjetos();

    }

    @Override
    public void render() {

        //Limpar frames anteriores
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT );

        verificarEstadoJogo();
        validarPontos();
        desenharObjetosTela();
        detectarColisoes();

    }

    /**
     * 0 - Jogo inicial, passaro parado
     * 1 - Começa o jogo
     * 2 - Colidiu
     */
    private void verificarEstadoJogo() {
        delayRender = Gdx.graphics.getDeltaTime();
        boolean toqueTela = Gdx.input.justTouched();

        if (estadoJogo == 0) {

            //Evento de clique na tela
            if (toqueTela) {
                gravidade = -15;
                estadoJogo = 1;
                somVoando.play();
            }

        } else if (estadoJogo == 1) {

            //Evento de clique na tela
            if (toqueTela) {
                gravidade = -15;
                somVoando.play();
            }

            //Movimentar cano
            posicaoCanoHorizontal -= delayRender * velocidadeJogo;
            if (posicaoCanoHorizontal < -canoTopo.getWidth()) {
                posicaoCanoHorizontal = larguraTela;
                posicaoCanoVertical = random.nextInt(800) - 400;

                passouCano = false;
            }
            //Gravidade do jogo
            if (possicaoInicialVerticalPassaro > 0 || gravidade < 0) {
                possicaoInicialVerticalPassaro = possicaoInicialVerticalPassaro - gravidade;
            }
            gravidade++;

        } else if (estadoJogo == 2) {

            //
            if ( pontos > recorde ){
               recorde = pontos;
               preferences.putInteger("recorde", recorde);
            }

            posicaoHorizontalPassaro = posicaoHorizontalPassaro - delayRender * 500;

            //Evento de clique na tela
            if (toqueTela) {
                estadoJogo = 0;
                pontos = 0;
                gravidade = 0;
                posicaoHorizontalPassaro = 0;
                possicaoInicialVerticalPassaro = alturaTela/2;
                posicaoCanoHorizontal = larguraTela;
            }
        }
    }

    private void detectarColisoes() {

        circuloPassaro.set(50 + posicaoHorizontalPassaro + passaros[0].getWidth() / 2, possicaoInicialVerticalPassaro + passaros[0].getHeight() / 2, passaros[0].getWidth() / 2);

        rectanguloCanoBaixo.set(
                posicaoCanoHorizontal,
                alturaTela / 2 - canoBaixo.getHeight() - espacoEntreCanos / 2 + posicaoCanoVertical,
                canoBaixo.getWidth(),
                canoBaixo.getHeight()
        );

        rectanguloCanoCima.set(
                posicaoCanoHorizontal,
                alturaTela / 2 + espacoEntreCanos / 2 + posicaoCanoVertical,
                canoTopo.getWidth(),
                canoTopo.getHeight()

        );

        boolean colidiuCanoCima = Intersector.overlaps(circuloPassaro, rectanguloCanoCima);
        boolean colidiuCanoBaixo = Intersector.overlaps(circuloPassaro, rectanguloCanoBaixo);

        //Verifica se houve colisão com o cano de cima
        if (colidiuCanoCima || colidiuCanoBaixo) {
            if (estadoJogo == 1){
                somColisao.play();
                estadoJogo = 2;
            }
        }
    }

    private void desenharObjetosTela() {

        batch.setProjectionMatrix( camera.combined );

        batch.begin();

        batch.draw(fundo, 0, 0, larguraTela, alturaTela);//tela de fundo
        batch.draw(passaros[(int) variacao], 50 + posicaoHorizontalPassaro, possicaoInicialVerticalPassaro);//passaro batendo asas
        batch.draw(canoBaixo, posicaoCanoHorizontal, alturaTela / 2 - canoBaixo.getHeight() - espacoEntreCanos / 2 + posicaoCanoVertical);
        batch.draw(canoTopo, posicaoCanoHorizontal, alturaTela / 2 + espacoEntreCanos / 2 + posicaoCanoVertical);

        textoPontuação.draw(batch, String.valueOf(pontos), larguraTela / 2, alturaTela - 100);

        if (estadoJogo == 2){
            batch.draw(gameOver, larguraTela/2 - gameOver.getWidth()/2, alturaTela/2);

            textoReiniciar.draw(batch, "Toque para reiniciar!", larguraTela/2 - 140, alturaTela/2 - gameOver.getHeight()/2);
            textoMelhorPontuação.draw(batch, "Seu recorder é: " + recorde + " pontos!", larguraTela/2 - 180, alturaTela/2 - gameOver.getHeight());
        }

        batch.end();

    }

    /**
     * Configuração das imagens do jogo
     */
    private void inicializarTextura() {
        passaros = new Texture[3];
        passaros[0] = new Texture("passaro1.png");
        passaros[1] = new Texture("passaro2.png");
        passaros[2] = new Texture("passaro3.png");
        fundo = new Texture("fundo.png");

        canoBaixo = new Texture("cano_baixo_maior.png");
        canoTopo = new Texture("cano_topo_maior.png");

        gameOver = new Texture("game_over.png");
    }

    /**
     * Configuração dos objetos do jogo
     */
    private void inicializarObjetos() {
        batch = new SpriteBatch();
        random = new Random();

        larguraTela = VIRTUAL_WIDTH;
        alturaTela = VIRTUAL_HEIGTH;

        possicaoInicialVerticalPassaro = alturaTela / 2;

        posicaoCanoHorizontal = larguraTela;
        espacoEntreCanos = 300;

        //Configurar textos
        textoPontuação = new BitmapFont();
        textoPontuação.setColor(Color.WHITE);
        textoPontuação.getData().setScale(10);

        textoReiniciar = new BitmapFont();
        textoReiniciar.setColor(Color.GREEN);
        textoReiniciar.getData().setScale(2);

        textoMelhorPontuação = new BitmapFont();
        textoMelhorPontuação.setColor(Color.RED);
        textoMelhorPontuação.getData().setScale(2);

        //Formas Geometricas para colisoes
        shapeRenderer = new ShapeRenderer();
        circuloPassaro = new Circle();
        rectanguloCanoBaixo = new Rectangle();
        rectanguloCanoCima = new Rectangle();

        //Iniciar sons
        somVoando = Gdx.audio.newSound( Gdx.files.internal("som_asa.wav"));
        somColisao = Gdx.audio.newSound( Gdx.files.internal("som_batida.wav"));
        somPontuacao = Gdx.audio.newSound( Gdx.files.internal("som_pontos.wav"));

        //Configurara preferencias
        preferences = Gdx.app.getPreferences("flappyBird");
        recorde = preferences.getInteger("recorde", 0);

        //Configuração da camera de visao
        camera = new OrthographicCamera();
        camera.position.set(VIRTUAL_WIDTH/2, VIRTUAL_HEIGTH/2, 0);
        viewport = new StretchViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGTH, camera);


    }

    /**
     * atualiza campo de visão do jogo no oncreat
     * @param width
     * @param height
     */
    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
    }

    private void validarPontos() {

        if (posicaoCanoHorizontal < 50 - passaros[0].getWidth()) { //passou do passaro
            if (!passouCano) {
                pontos += 1;
                passouCano = true;
                somPontuacao.play();
                if (pontos == 5)velocidadeJogo += 100;
                if (pontos == 10)velocidadeJogo += 100;
                if (pontos == 15)velocidadeJogo += 100;
            }
        }

        variacao += delayRender * 10;
        //Verifica variação para bater asas
        if (variacao > 3) {
            variacao = 0;
        }
    }

    @Override
    public void dispose() {

    }
}
