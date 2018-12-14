# Udacity Android Developer Nanodegree - Projeto 2 (Popular Movies - Stage 2)

Este é um projeto dividido em 2 etapas. Nesta segunda etapa, temos a evolução do app contruído na [primeira](https://github.com/oliveira-marcio/adnd-popular-movies-stage1) onde, além de consultar a [The Movie Database API](https://developers.themoviedb.org/3) e exibir uma grade com diversos posters de filmes ordenados pelos mais populares ou melhores avaliados, o usuário agora irá poder salvar seus filmes favoritos e exibí-los numa visualização à parte. O app também sincroniza todos os dados de filmes com um banco de dados local para consulta offline.

Além disso, a tela de detalhes de um filme agora exibe também links para os trailers e lista de comentários feitos por usuários.

O app continua utilizando a biblioteca [Picasso](http://square.github.io/picasso/) para obtenção e cache das imagens e componentes nativos do framework Android para as demais funcionalidades, como `Loader`, `HttpURLConnection`, `InputStream`, `JSONObject`, entre outros e passou a utilizar `IntentServices` para fazer a sincronia de dados num serviço em background e `ContentProvider` para gerenciar os dados e atualização da UI.

**OBS:** A primeira etapa do projeto pode ser acessada [aqui](https://github.com/oliveira-marcio/adnd-popular-movies-stage1).

## Instalação:
- Faça um clone do repositório
- Importe a pasta como um novo projeto no [Android Studio](https://developer.android.com/studio/)
- Crie uma chave de developer na **The Movie Database API**. Instruções [aqui](https://www.themoviedb.org/settings/api).
- Crie (ou edite) o arquivo `gradle.properties` na raiz do projeto e adicione a chave da API:
`MyTMDBApiKey="xxxxxxxxxxxxxx"`
- Configure um [emulador](https://developer.android.com/studio/run/emulator) ou conecte um [celular com USB debug ativado](https://developer.android.com/studio/run/device)
- Execute apartir do menu "Run"

## Copyright

Esse projeto foi desenvolvido por Márcio Souza de Oliveira em 16/03/2017.

