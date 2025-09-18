package com.monstrous.dungeon;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalShadowLight;
import com.badlogic.gdx.graphics.g3d.shaders.DepthShader;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.ScreenUtils;
import com.github.xpenatan.webgpu.WGPUTextureFormat;
import com.monstrous.dungeon.render.SceneManager;
import com.monstrous.gdx.webgpu.graphics.WgTexture;
import com.monstrous.gdx.webgpu.graphics.utils.WgFrameBuffer;


/** Utility class to render a game object to generate an icon sprite.  These icons are used in the inventory. */
public class ShowCase implements Disposable {
    final static int SHADOW_MAP_SIZE = 2048;

    private SceneManager sceneManager;
    private PerspectiveCamera camera;
    private Cubemap diffuseCubemap;
    private Cubemap environmentCubemap;
    private Cubemap specularCubemap;
    private Texture brdfLUT;
    private DirectionalLight light;
    private DirectionalShadowLight shadowCastingLight;
    private WgFrameBuffer fbo;
    //private Filter filter;

    public ShowCase() {

        sceneManager = new SceneManager();
        sceneManager.fogColor = Color.DARK_GRAY;
        float ambientLevel = 0.5f;
        ColorAttribute ambient =  ColorAttribute.createAmbientLight(ambientLevel, ambientLevel, ambientLevel, 1f);
        sceneManager.environment.set(ambient);

        camera = new PerspectiveCamera(80, 64, 64); //Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        camera.position.set(0.3f, 0.1f, 0.3f);
        camera.lookAt(0,0,0);
        camera.near = 0.001f;
        camera.far = 100f;
        camera.update();

        // setup camera
//        camera = new OrthographicCamera();
//        camera.near = 0.1f;
//        camera.far = 500;
//        camera.position.set(5,5, 5);
//        camera.zoom = 0.025f;
//        camera.up.set(Vector3.Y);
//        camera.lookAt( new Vector3(0, 0f, 0));
//
//        camera.update();
        //sceneManager.setCamera(camera);



        // setup light

        // setup shadow light
//        shadowCastingLight = new DirectionalShadowLight(SHADOW_MAP_SIZE, SHADOW_MAP_SIZE);
//        shadowCastingLight.direction.set(-3, -2, -3).nor();
//        shadowCastingLight.color.set(Color.YELLOW);
//        shadowCastingLight.intensity = .5f;
//        shadowCastingLight.setViewport(64, 64, 0.1f, 50f);
//
//        sceneManager.environment.add(shadowCastingLight);
//        sceneManager.environment.set(new PBRFloatAttribute(PBRFloatAttribute.ShadowBias, 1f/256f));
//
//        // setup quick IBL (image based lighting)
//        IBLBuilder iblBuilder = IBLBuilder.createOutdoor(shadowCastingLight);
//        environmentCubemap = iblBuilder.buildEnvMap(1024);
//        diffuseCubemap = iblBuilder.buildIrradianceMap(256);
//        specularCubemap = iblBuilder.buildRadianceMap(10);
//        iblBuilder.dispose();
//
//        // This texture is provided by the library, no need to have it in your assets.
//        brdfLUT = new Texture(Gdx.files.classpath("net/mgsx/gltf/shaders/brdfLUT.png"));

//        sceneManager.setAmbientLight(0.1f);
//        sceneManager.environment.set(new PBRTextureAttribute(PBRTextureAttribute.BRDFLUTTexture, brdfLUT));
//        sceneManager.environment.set(PBRCubemapAttribute.createSpecularEnv(specularCubemap));
//        sceneManager.environment.set(PBRCubemapAttribute.createDiffuseEnv(diffuseCubemap));
//
//        filter = new Filter();
    }
    private BoundingBox bbox = new BoundingBox();

    public Sprite makeIcon(Model asset, int w, int h, boolean high ){

        // asset can be null to get an empty icon
        ModelInstance scene = null;
        if(asset != null) {
            asset.calculateBoundingBox(bbox);
            scene = new ModelInstance(asset, 0, -bbox.getHeight()/2, 0);
            sceneManager.add(scene);
        }

//        if(high)
//            camera.lookAt( new Vector3(0, 1f, 0));
//        else
            camera.lookAt( new Vector3(0, 0f, 0));
        camera.update();



        fbo = new WgFrameBuffer(w, h, true);

//        sceneManager.updateViewport(w, h);
//        filter.resize(w, h);

        // render
        //sceneManager.renderShadows();
        fbo.begin();
        //ScreenUtils.clear(Color.BLACK, true);
        sceneManager.render(camera);
        fbo.end();

//        Pixmap pixmap = new Pixmap(w, h, Pixmap.Format.RGBA8888);
//        pixmap.setColor(Color.CHARTREUSE);
//        pixmap.fill();
//        WgTexture texture = new WgTexture(pixmap);
//        //Sprite icon = new Sprite(texture);

        //do we need to make a copy of the texture?
        Sprite sprite = new Sprite(fbo.getColorBufferTexture());
        //sprite.flip(false, true); // coordinate system in buffer differs from screen

        //filter.render(fbo,0,0, w, h);

        sceneManager.clear();

        return sprite;
    }

    @Override
    public void dispose() {
        sceneManager.dispose();
        environmentCubemap.dispose();
        diffuseCubemap.dispose();
        specularCubemap.dispose();
        brdfLUT.dispose();
        //filter.dispose();
        fbo.dispose();
    }
}
